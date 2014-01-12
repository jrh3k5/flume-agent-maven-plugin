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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.github.jrh3k5.mojo.flume.io.ArchiveUtils;
import com.github.jrh3k5.mojo.flume.io.FlumeArchiveCache;
import com.github.jrh3k5.mojo.flume.io.FlumeCopier;
import com.github.jrh3k5.mojo.flume.process.AgentProcess;

/**
 * Unit tests for {@link AbstractFlumeAgentMojo}.
 * 
 * @author Joshua Hyde
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AbstractFlumeAgentMojo.class, AgentProcess.class, ArchiveUtils.class, FlumeArchiveCache.class, FlumeCopier.class })
public class AbstractFlumeAgentMojoTest extends AbstractUnitTest {
    private final String javaOpts = "-Xmx20m";
    private final String agentName = UUID.randomUUID().toString();
    private final AbstractFlumeAgentMojo mojo = new ConcreteMojo();
    @Mock
    private File configFile;
    @Mock
    private DependencyGraphBuilder dependencyGraphBuilder;
    @Mock
    private MavenProject project;
    @Mock
    private ArtifactResolver artifactResolver;
    private File outputDirectory;
    private URL flumeArchiveUrl;

    /**
     * Set up the plugin for each test.
     * 
     * @throws Exception
     *             If there are any errors while setting up the mojo.
     */
    @Before
    public void setUpMojo() throws Exception {
        flumeArchiveUrl = URI.create("http://localhost:8080/apache-flume-1.4.0-bin.tar.gz").toURL();
        outputDirectory = createTestDirectory();
        setParameters(mojo);
    }

    /**
     * Test the building of the agent process.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testBuildAgentProcess() throws Exception {
        final File flumeDirectory = mock(File.class);

        final AgentProcess.Builder agentProcessBuilder = mock(AgentProcess.Builder.class);
        mockStatic(AgentProcess.class);
        when(AgentProcess.newBuilder(flumeDirectory)).thenReturn(agentProcessBuilder);

        final AgentProcess agentProcess = mock(AgentProcess.class);
        when(agentProcessBuilder.withAgent(agentName)).thenReturn(agentProcessBuilder);
        when(agentProcessBuilder.withConfigFile(configFile)).thenReturn(agentProcessBuilder);
        when(agentProcessBuilder.build()).thenReturn(agentProcess);

        final AtomicBoolean copiedPlugins = new AtomicBoolean(false);
        final AtomicBoolean unpackedFlume = new AtomicBoolean(false);
        final AtomicBoolean wroteFlumeEnvironment = new AtomicBoolean(false);

        final ConcreteMojo toTest = setParameters(new ConcreteMojo() {
            @Override
            void copyFlumePlugins(File givenFlumeDirectory) throws IOException {
                copiedPlugins.set(true);
                assertThat(givenFlumeDirectory).isEqualTo(flumeDirectory);
            }

            @Override
            File unpackFlume(FlumeArchiveCache archiveCache) throws IOException {
                unpackedFlume.set(true);
                return flumeDirectory;
            }

            @Override
            void writeFlumeEnvironment(File givenFlumeDirectory) throws IOException {
                wroteFlumeEnvironment.set(true);
                assertThat(givenFlumeDirectory).isEqualTo(flumeDirectory);
            }
        });

        assertThat(toTest.buildAgentProcess()).isEqualTo(agentProcess);

        assertThat(copiedPlugins.get()).isTrue();
        assertThat(unpackedFlume.get()).isTrue();
        assertThat(wroteFlumeEnvironment.get()).isTrue();
    }

    /**
     * Test the copying of Flume plugins.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testCopyFlumePlugins() throws Exception {
        final File flumeDirectory = createTestDirectory();
        FileUtils.forceMkdir(flumeDirectory);

        // Build the file that is to be copied and the temporarily-staged .tar file from it
        final String randomUuid = UUID.randomUUID().toString();
        final File flumePluginFile = new File(flumeDirectory, randomUuid + ".tar.gz");
        final File flumePluginTarFile = new File(flumeDirectory, randomUuid + ".tar");

        // Create a collection that will be used to inject the "discovered" Flume plugin dependencies
        final Artifact flumePluginArtifact = mock(Artifact.class);
        when(flumePluginArtifact.getFile()).thenReturn(flumePluginFile);
        final Collection<Artifact> flumePluginDependencies = Collections.singleton(flumePluginArtifact);

        // So that we don't have to *actually* test the untarring/unzipping here
        mockStatic(ArchiveUtils.class);

        final ConcreteMojo toTest = setParameters(new ConcreteMojo() {
            @Override
            Collection<Artifact> getFlumePluginDependencies() throws IOException {
                return flumePluginDependencies;
            }
        });
        // Without this, the method will think there are no Flume plugins to copy
        setFlumePlugins(toTest, Collections.<FlumePlugin> singleton(mock(FlumePlugin.class)));

        // Actually invoke the method to be tested
        toTest.copyFlumePlugins(flumeDirectory);

        final File expectedPluginsDir = new File(flumeDirectory, "plugins.d");
        // The "discovered" Flume plugin should have been "unzipped" and "untarred"
        verifyStatic();
        ArchiveUtils.gunzipFile(flumePluginFile.toURI().toURL(), flumePluginTarFile);
        ArchiveUtils.untarFile(flumePluginTarFile, expectedPluginsDir);
    }

    /**
     * If there are no Flume plugins explicitly configured, then copying Flume plugins should have no effect.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testCopyFlumePluginsNoneSet() throws Exception {
        final ConcreteMojo toTest = setParameters(new ConcreteMojo() {
            @Override
            Collection<Artifact> getFlumePluginDependencies() throws IOException {
                throw new IllegalStateException("This should never get invoked.");
            }
        });
        // The absence of errors indicates that nothing happened
        toTest.copyFlumePlugins(createTestDirectory());
    }

    /**
     * Test the retrieval of Flume plugins from the project dependency set.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testGetFlumePluginDependencies() throws Exception {
        final ArgumentCaptor<ArtifactFilter> artifactFilterCaptor = ArgumentCaptor.forClass(ArtifactFilter.class);
        final ArgumentCaptor<ArtifactResolutionRequest> resolutionRequestCaptor = ArgumentCaptor.forClass(ArtifactResolutionRequest.class);

        // Populate the list of Flume plugins for testing of the artifact filter
        final FlumePlugin flumePlugin = mock(FlumePlugin.class);
        setFlumePlugins(mojo, Collections.<FlumePlugin> singleton(flumePlugin));

        // Build the node that represents the project itself
        final DependencyNode rootNode = mock(DependencyNode.class);
        when(dependencyGraphBuilder.buildDependencyGraph(eq(project), artifactFilterCaptor.capture())).thenReturn(rootNode);

        // Build the list of dependencies in the project itself
        final DependencyNode childNode = mock(DependencyNode.class);
        final List<DependencyNode> childrenNodes = Collections.singletonList(childNode);
        when(rootNode.getChildren()).thenReturn(childrenNodes);

        // Build the actual dependency information
        final Artifact childArtifact = mock(Artifact.class);
        when(childNode.getArtifact()).thenReturn(childArtifact);

        final ArtifactResolutionResult resolutionResult = mock(ArtifactResolutionResult.class);
        when(artifactResolver.resolve(resolutionRequestCaptor.capture())).thenReturn(resolutionResult);

        assertThat(mojo.getFlumePluginDependencies()).hasSize(1).contains(childArtifact);

        // Verify the internals - first, the construction of the resolution request
        final ArtifactResolutionRequest resolutionRequest = resolutionRequestCaptor.getValue();
        assertThat(resolutionRequest.getArtifact()).isEqualTo(childArtifact);

        // Test the artifact filter
        final ArtifactFilter artifactFilter = artifactFilterCaptor.getValue();
        when(flumePlugin.matches(childArtifact)).thenReturn(Boolean.TRUE);
        assertThat(artifactFilter.include(childArtifact)).isTrue();
        assertThat(artifactFilter.include(mock(Artifact.class))).isFalse();
    }

    /**
     * Test the removal of libraries from the Flume installation.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testRemoveLibs() throws Exception {
        final File testDirectory = createTestDirectory();
        final File libDir = new File(testDirectory, "lib");
        FileUtils.forceMkdir(libDir);

        final File toRemove = new File(libDir, "toremove.jar");
        FileUtils.touch(toRemove);
        final File toKeep = new File(libDir, "tokeep.jar");
        FileUtils.touch(toKeep);

        final Libs libs = Whitebox.getInternalState(mojo, "libs");
        libs.setRemovals(Collections.singletonList(toRemove.getName()));

        // The mojo should remove only the configured library
        mojo.removeLibs(testDirectory);

        assertThat(toRemove).doesNotExist();
        assertThat(toKeep).exists();
    }

    /**
     * Test the unpacking of Flume.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testUnpackFlume() throws Exception {
        final URL resolvedUrl = new File(createTestDirectory(), UUID.randomUUID().toString() + ".tar.gz").toURI().toURL();
        final FlumeArchiveCache archiveCache = mock(FlumeArchiveCache.class);
        whenNew(FlumeArchiveCache.class).withArguments(flumeArchiveUrl).thenReturn(archiveCache);
        when(archiveCache.getArchiveLocation()).thenReturn(resolvedUrl);

        final File flumeDirectory = new File(createTestDirectory(), "flume");
        final FlumeCopier flumeCopier = mock(FlumeCopier.class);
        when(flumeCopier.copyTo(outputDirectory)).thenReturn(flumeDirectory);
        whenNew(FlumeCopier.class).withArguments(archiveCache).thenReturn(flumeCopier);
        assertThat(mojo.unpackFlume(archiveCache)).isEqualTo(flumeDirectory);
    }

    /**
     * Test writing the Flume environment.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testWriteFlumeEnvironment() throws Exception {
        final ConcreteMojo toTest = setParameters(new ConcreteMojo());
        final File flumeDirectory = new File(createTestDirectory(), "flume");
        toTest.writeFlumeEnvironment(flumeDirectory);
        final File confDirectory = new File(flumeDirectory, "conf");
        assertThat(confDirectory).exists();
        final File flumeEnvSh = new File(confDirectory, "flume-env.sh");
        assertThat(flumeEnvSh).exists();
        assertThat(FileUtils.readFileToString(flumeEnvSh, "utf-8")).isEqualTo(String.format("JAVA_OPTS=\"%s\"", javaOpts));
    }

    /**
     * Decorate the internals of the given plugin with usable values.
     * 
     * @param mojo
     *            The {@link AbstractFlumeAgentMojo} to be decorated.
     * @return The given mojo class.
     */
    private <T extends AbstractFlumeAgentMojo> T setParameters(T mojo) {
        Whitebox.setInternalState(mojo, "agentName", agentName);
        Whitebox.setInternalState(mojo, "configFile", configFile);
        Whitebox.setInternalState(mojo, "dependencyGraphBuilder", dependencyGraphBuilder);
        Whitebox.setInternalState(mojo, "project", project);
        Whitebox.setInternalState(mojo, "artifactResolver", artifactResolver);
        Whitebox.setInternalState(mojo, "outputDirectory", outputDirectory);
        Whitebox.setInternalState(mojo, "javaOpts", javaOpts);
        Whitebox.setInternalState(mojo, "outputEncoding", "utf-8");
        Whitebox.setInternalState(mojo, "flumeArchiveUrl", flumeArchiveUrl);
        return mojo;
    }

    /**
     * Set the stored Flume plugins in the given
     * 
     * @param mojo
     * @param flumePlugins
     * @return
     */
    private <T extends AbstractFlumeAgentMojo> T setFlumePlugins(T mojo, Collection<FlumePlugin> flumePlugins) {
        Whitebox.setInternalState(mojo, "flumePlugins", Collections.unmodifiableList(flumePlugins instanceof List ? (List<FlumePlugin>) flumePlugins : new ArrayList<FlumePlugin>(flumePlugins)));
        return mojo;
    }

    /**
     * Simple implementation of {@link AbstractFlumeAgentMojo} for testing purposes.
     * 
     * @author jh016266
     * @since 4.0
     */
    private static class ConcreteMojo extends AbstractFlumeAgentMojo {
        @Override
        public void execute() throws MojoExecutionException, MojoFailureException {
            // no-op
        }
    }
}
