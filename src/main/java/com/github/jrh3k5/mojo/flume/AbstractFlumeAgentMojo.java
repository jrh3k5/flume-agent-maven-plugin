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
 */

public abstract class AbstractFlumeAgentMojo extends AbstractMojo {
    /**
     * The name of the agent to be executed.
     */
    @Parameter(required = true)
    private String agentName;

    /**
     * The configuration file to be supplied to the agent.
     */
    @Parameter(required = true)
    private File configFile;

    /**
     * The directory to which the installation of the Flume agent should be extracted.
     */
    @Parameter(defaultValue = "${project.build.directory}")
    private File outputDirectory;

    /**
     * Definition of objects that will be unpacked and copied as plugins for Flume into the installation directory.
     * <p />
     * Configurations of this can look like:
     * 
     * <pre>
     *  &lt;flumePlugins&gt;
     *      &lt;flumePlugin&gt;
     *          &lt;groupId&gt;com.cerner.test&lt;/groupId&gt;
     *          &lt;artifactId&gt;some-flume-plugin&lt;/artifactId&gt;
     *      &lt;/flumePlugin&gt;
     *  &lt;/flumePlugins&gt;
     * </pre>
     * 
     * This will match a dependency defined as:
     * 
     * <pre>
     *  &lt;dependency&gt;
     *      &lt;groupId&gt;com.cerner.test&lt;/groupId&gt;
     *      &lt;artifactId&gt;some-flume-plugin&lt;/artifactId&gt;
     *      &lt;classifier&gt;flume-plugin&lt;/classifier&gt;
     *      &lt;type&gt;tar.gz&lt;/type&gt;
     *  &lt;/dependency&gt;
     * </pre>
     * 
     * Please be conscious of the expected classifier and type.
     */
    @Parameter
    private List<FlumePlugin> flumePlugins = Collections.emptyList();

    /**
     * The value to be set as the {@code JAVA_OPTS} parameter in the Flume environment.
     */
    @Parameter(required = true, defaultValue = "-Xmx20m")
    private String javaOpts;

    /**
     * The encoding to be used when writing any text data to disk.
     */
    @Parameter(required = true, defaultValue = "${project.build.sourceEncoding}")
    private String outputEncoding;

    /**
     * The URL from which the Flume binary archive should be downloaded.
     */
    @Parameter(required = true, defaultValue = "http://archive.apache.org/dist/flume/1.4.0/apache-flume-1.4.0-bin.tar.gz")
    private URL flumeArchiveUrl;

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
     * Get the name of the agent to be executed.
     * 
     * @return The name of the agent.
     */
    protected String getAgentName() {
        return agentName;
    }

    /**
     * Get the location of the configuration file for the Flume agent.
     * 
     * @return A {@link File} used to describe the location of the configuration file for the Flume agent.
     */
    protected File getConfigFile() {
        return configFile;
    }

    /**
     * Build the agent process.
     * 
     * @return An {@link AgentProcess} describing the agent process.
     * @throws MojoExecutionException
     *             If any errors occur while building the agent process.
     */
    protected AgentProcess buildAgentProcess() throws MojoExecutionException {
        File flumeDirectory;
        try {
            flumeDirectory = unpackFlume(new FlumeArchiveCache(flumeArchiveUrl));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to unpack Flume.", e);
        }
        try {
            copyFlumePlugins(flumeDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to copy all Flume plugins.", e);
        }
        try {
            writeFlumeEnvironment(flumeDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Error writing Flume environment to directory: " + flumeDirectory.getAbsolutePath(), e);
        }
        final AgentProcess.Builder builder = AgentProcess.newBuilder(flumeDirectory);
        return builder.withAgent(getAgentName()).withConfigFile(getConfigFile()).build();
    }

    /**
     * Copy any configured Flume plugins to the given Flume installation directory.
     * <p />
     * This is intentionally made package-private to expose it for testing purposes.
     * 
     * @param flumeDirectory
     *            A {@link File} representing the directory in which Flume is installed.
     * @throws IOException
     *             If any errors occur during the copying.
     */
    void copyFlumePlugins(File flumeDirectory) throws IOException {
        if (flumePlugins.isEmpty()) {
            return;
        }

        final File pluginsDir = new File(flumeDirectory, "plugins.d");
        for (Artifact pluginArtifact : getFlumePluginDependencies()) {
            final URL pluginUrl = pluginArtifact.getFile().toURI().toURL();
            final File tarFile = removeFinalExtension(pluginArtifact.getFile());
            gunzipFile(pluginUrl, tarFile);
            untarFile(tarFile, pluginsDir);
        }
    }

    /**
     * Retrieve from the current project all Flume plugins declared as dependencies.
     * <p />
     * This is intentionally made package-private to expose it for testing purposes.
     * 
     * @return A {@link Collection} of {@link Artifact} objects representing the given Flume plugins as project dependencies.
     * @throws IOException
     *             If any errors occur during the plugin retrieval.
     */
    Collection<Artifact> getFlumePluginDependencies() throws IOException {
        try {
            final DependencyNode rootNode = dependencyGraphBuilder.buildDependencyGraph(project, new FlumePluginsArtifactFilter(flumePlugins));
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
     * Unpack the Flume installation.
     * <p />
     * This is intentionally made package-private to expose it for testing purposes.
     * 
     * @param archiveCache
     *            A {@link FlumeArchiveCache} that informs the plugin of where to get the Flume archive.
     * @return A {@link File} representing the location of the Flume installation.
     * @throws IOException
     *             If any errors occur during the unpacking.
     */
    File unpackFlume(FlumeArchiveCache archiveCache) throws IOException {
        return new FlumeCopier(archiveCache).copyTo(outputDirectory);
    }

    /**
     * Write the Flume environment to the configuration directory.
     * 
     * @param flumeDirectory
     *            A {@link File} representing the directory to which the Flume environment configuration will be written.
     * @throws IOException
     *             If any errors occur while writing the Flume environment.
     */
    void writeFlumeEnvironment(File flumeDirectory) throws IOException {
        final File confDir = new File(flumeDirectory, "conf");
        FileUtils.forceMkdir(confDir);
        FileUtils.fileWrite(new File(confDir, "flume-env.sh"), outputEncoding, String.format("JAVA_OPTS=\"%s\"", javaOpts));
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
     * @author jh016266
     * @since 4.0
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