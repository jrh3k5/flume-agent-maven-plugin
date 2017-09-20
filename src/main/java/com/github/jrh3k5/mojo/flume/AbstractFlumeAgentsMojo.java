/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jrh3k5.mojo.flume;

import static com.github.jrh3k5.mojo.flume.io.ArchiveUtils.gunzipFile;
import static com.github.jrh3k5.mojo.flume.io.ArchiveUtils.untarFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.codehaus.plexus.util.FileUtils;

import com.github.jrh3k5.mojo.flume.io.FlumeArchiveCache;
import com.github.jrh3k5.mojo.flume.io.FlumeCopier;
import com.github.jrh3k5.mojo.flume.process.AgentProcess;

/**
 * Abstract definition of a mojo that manages a Flume agent.
 * 
 * @author Joshua Hyde
 * @since 2.0
 */

public abstract class AbstractFlumeAgentsMojo extends AbstractMojo {
    /**
     * The directory to which the installation of the Flume agent should be extracted.
     */
    @Parameter(defaultValue = "${project.build.directory}/apache-flume")
    private File outputDirectory;

    /**
     * The encoding to be used when writing any text data to disk.
     */
    @Parameter(required = true, defaultValue = "${project.build.sourceEncoding}")
    private String outputEncoding;

    /**
     * The URL from which the Flume binary archive should be downloaded.
     */
    @Parameter(required = true, defaultValue = "http://archive.apache.org/dist/flume/1.7.0/apache-flume-1.7.0-bin.tar.gz")
    private URL flumeArchiveUrl;

    /**
     * The MD5 hash of the Flume archive to be downloaded.
     * <p />
     * Be aware that Flume's released MD5 hashes are signed using PGP; this mojo does <b>not</b> support verification of those hashes.
     * 
     * @since 2.1
     */
    @Parameter(required = true, defaultValue = "12496e632a96d7ca823ab3c239a2a7d2")
    private String flumeArchiveMd5;

    /**
     * The Maven project descriptor.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * An {@link ArtifactResolver} used to copy dependencies.
     */
    @Component
    private ArtifactResolver artifactResolver;

    /**
     * A {@link DependencyGraphBuilder} used to assemble the dependency graph of the project consuming this plugin.
     */
    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;

    /**
     * The configuration of agents to be run, expressed as {@link Agent} objects.
     */
    @Parameter(required = true)
    private List<Agent> agents = Collections.emptyList();

    /**
     * Get the agents configured for the plugin.
     * 
     * @return A {@link List} of {@link Agent} objects representing the agents configured for the plugin.
     * @since 2.0
     */
    protected List<Agent> getAgents() {
        return Collections.unmodifiableList(agents);
    }

    /**
     * Build the agent process.
     * 
     * @param agent
     *            The {@link Agent} for which a process is to be built.
     * @return An {@link AgentProcess} describing the agent process.
     * @throws MojoExecutionException
     *             If any errors occur while building the agent process.
     */
    protected AgentProcess buildAgentProcess(Agent agent) throws MojoExecutionException {
        File flumeDirectory;
        try {
            flumeDirectory = unpackFlume(agent, new FlumeArchiveCache(flumeArchiveUrl, flumeArchiveMd5));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to unpack Flume.", e);
        }
        try {
            copyFlumePlugins(agent, flumeDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to copy all Flume plugins.", e);
        }
        try {
            copyLoggingProperties(agent, flumeDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to copy the Flume logging properties.", e);
        }
        try {
            writeFlumeEnvironment(agent, flumeDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Error writing Flume environment to directory: " + flumeDirectory.getAbsolutePath(), e);
        }
        try {
            removeLibs(agent, flumeDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to remove libs.", e);
        }
        final AgentProcess.Builder builder = AgentProcess.newBuilder(flumeDirectory);
        return builder.withAgent(agent.getAgentName()).withConfigFile(agent.getConfigFile()).build();
    }

    /**
     * Copy any configured Flume plugins to the given Flume installation directory.
     * <p />
     * This is intentionally made package-private to expose it for testing purposes.
     * 
     * @param agent
     *            The {@link Agent} whose Flume plugins are to be installed.
     * @param flumeDirectory
     *            A {@link File} representing the directory in which Flume is installed.
     * @throws IOException
     *             If any errors occur during the copying.
     */
    void copyFlumePlugins(Agent agent, File flumeDirectory) throws IOException {
        if (agent.getFlumePlugins().isEmpty()) {
            return;
        }

        final File pluginsDir = new File(flumeDirectory, "plugins.d");
        for (Artifact pluginArtifact : getFlumePluginDependencies(agent)) {
            final URL pluginUrl = pluginArtifact.getFile().toURI().toURL();
            final File tarFile = removeFinalExtension(pluginArtifact.getFile());
            gunzipFile(pluginUrl, tarFile);
            untarFile(tarFile, pluginsDir);
        }
    }

    /**
     * Copy the configured logging properties, if provided, into the Flume installation.
     * 
     * @param agent
     *            An {@link Agent} object describing the agent configuration.
     * @param flumeDirectory
     *            A {@link File} object representing the location of the Flume installation.
     * @throws IOException
     *             If any errors occur during the copying.
     * @since 2.1.1
     */
    void copyLoggingProperties(Agent agent, File flumeDirectory) throws IOException {
        if (agent.getLoggingProperties() == null) {
            return;
        }

        final File confDir = new File(flumeDirectory, "conf");
        FileUtils.copyFile(agent.getLoggingProperties(), new File(confDir, "log4j.properties"));
    }

    /**
     * Retrieve from the current project all Flume plugins declared as dependencies.
     * <p />
     * This is intentionally made package-private to expose it for testing purposes.
     * 
     * @param agent
     *            The {@link Agent} whose Flume plugins are to be resolved.
     * @return A {@link Collection} of {@link Artifact} objects representing the given Flume plugins as project dependencies.
     * @throws IOException
     *             If any errors occur during the plugin retrieval.
     */
    Collection<Artifact> getFlumePluginDependencies(Agent agent) throws IOException {
        try {
            final DependencyNode rootNode = dependencyGraphBuilder.buildDependencyGraph(project, new FlumePluginsArtifactFilter(agent.getFlumePlugins()));
            final List<Artifact> artifacts = new ArrayList<Artifact>(rootNode.getChildren().size());
            for (DependencyNode childNode : rootNode.getChildren()) {
                final Artifact artifact = childNode.getArtifact();
                final ArtifactResolutionResult result = artifactResolver.resolve(toRequest(artifact));
                if (!result.getMissingArtifacts().isEmpty()) {
                    throw new IOException("Unable to resolve one or more artifacts: " + result.getMissingArtifacts());
                }
                artifacts.add(artifact);
            }
            return artifacts;
        } catch (DependencyGraphBuilderException e) {
            throw new IOException("Failed to find plugins as dependencies.", e);
        }
    }

    /**
     * Remove any libraries from the {@code lib/} directory in the given Flume installation directory.
     * 
     * @param agent
     *            The {@link Agent} whose installation's {@code lib/} directory is to be modified.
     * @param flumeDirectory
     *            A {@link File} representing the directory in which a Flume agent - for which the configured libraries are to be removed - is installed.
     * @throws IOException
     *             If any errors occur during the removal.
     */
    void removeLibs(Agent agent, File flumeDirectory) throws IOException {
        final File libDir = new File(flumeDirectory, "lib");
        final Log log = getLog();
        final boolean isDebugEnabled = log.isDebugEnabled();
        for (String removal : agent.getLibs().getRemovals()) {
            final File lib = new File(libDir, removal);
            if (lib.exists()) {
                if (isDebugEnabled) {
                    log.debug(String.format("The file %s exists and will be removed.", lib.getAbsolutePath()));
                }
                FileUtils.forceDelete(lib);
            } else {
                log.warn(String.format("The file %s was specified for deletion, but could not be found in %s", removal, libDir.getAbsolutePath()));
            }
        }
    }

    /**
     * Unpack the Flume installation.
     * <p />
     * This is intentionally made package-private to expose it for testing purposes.
     * 
     * @param agent
     *            The {@link Agent} for which the Flume installation is to be unpacked.
     * @param archiveCache
     *            A {@link FlumeArchiveCache} that informs the plugin of where to get the Flume archive.
     * @return A {@link File} representing the location of the Flume installation.
     * @throws IOException
     *             If any errors occur during the unpacking.
     */
    File unpackFlume(Agent agent, FlumeArchiveCache archiveCache) throws IOException {
        return new FlumeCopier(archiveCache).copyTo(getAgentDirectory(agent));
    }

    /**
     * Write the Flume environment to the configuration directory.
     * 
     * @param agent
     *            The {@link Agent} whose environment is to be modified.
     * @param flumeDirectory
     *            A {@link File} representing the directory to which the Flume environment configuration will be written.
     * @throws IOException
     *             If any errors occur while writing the Flume environment.
     */
    void writeFlumeEnvironment(Agent agent, File flumeDirectory) throws IOException {
        final File confDir = new File(flumeDirectory, "conf");
        FileUtils.forceMkdir(confDir);
        FileUtils.fileWrite(new File(confDir, "flume-env.sh"), outputEncoding, String.format("JAVA_OPTS=\"%s\"", agent.getJavaOpts()));
    }

    /**
     * Get the directory into which the agent will be installed.
     * 
     * @param agent
     *            The {@link Agent} whose installation directory is to be retrieved.
     * @return A {@link File} representing the directory into which the agent will be installed.
     * @throws IOException
     *             If any errors occur while creating the agent directory.
     */
    private File getAgentDirectory(Agent agent) throws IOException {
        final File agentDirectory = new File(outputDirectory, agent.getAgentName());
        if (!agentDirectory.exists()) {
            FileUtils.forceMkdir(agentDirectory);
        }
        return agentDirectory;
    }

    /**
     * Strip the tail file extension from the given file.
     * 
     * @param file
     *            A {@link File} from which the final file extension is to be removed.
     * @return A {@link File} representing a new file location, less the final file extension (if any).
     */
    private static File removeFinalExtension(File file) {
        final String absolutePath = file.getName();
        final int lastPeriodPos = absolutePath.lastIndexOf('.');
        if (lastPeriodPos < 0) {
            return file;
        }
        return new File(file.getParentFile(), absolutePath.substring(0, lastPeriodPos));
    }

    /**
     * Convert an {@link Artifact} into an {@link ArtifactResolutionRequest}.
     * 
     * @param artifact
     *            The {@link Artifact} to be converted into a resolution request.
     * @return An {@link ArtifactResolutionRequest} made out of the given artifact.
     */
    private ArtifactResolutionRequest toRequest(Artifact artifact) {
        final ArtifactResolutionRequest request = new ArtifactResolutionRequest();
        request.setArtifact(artifact);
        return request;
    }

    /**
     * An {@link ArtifactFilter} that filters out any artifact that don't match the given set of Flume plugins.
     * 
     * @author Joshua Hyde
     */
    private static class FlumePluginsArtifactFilter implements ArtifactFilter {
        private final Collection<FlumePlugin> flumePlugins;

        /**
         * Create a filter.
         * 
         * @param flumePlugins
         *            A {@link Collection} of {@link FlumePlugin} objects that represent the only allowable artifacts.
         */
        public FlumePluginsArtifactFilter(Collection<FlumePlugin> flumePlugins) {
            this.flumePlugins = Collections.unmodifiableCollection(flumePlugins);
        }

        @Override
        public boolean include(Artifact artifact) {
            for (FlumePlugin flumePlugin : flumePlugins) {
                if (flumePlugin.matches(artifact)) {
                    return true;
                }
            }
            return false;
        }
    }
}