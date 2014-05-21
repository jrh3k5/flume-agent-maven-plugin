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
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link FlumePlugin}.
 * 
 * @author Joshua Hyde
 */

@RunWith(MockitoJUnitRunner.class)
public class FlumePluginTest {
    private final String groupId = UUID.randomUUID().toString();
    private final String artifactId = UUID.randomUUID().toString();
    private final String classifier = "flume-plugin";
    private final String type = "tar.gz";
    private final FlumePlugin flumePlugin = new FlumePlugin();
    @Mock
    private Artifact artifact;

    @Before
    public void setUp() {
        flumePlugin.setGroupId(groupId);
        flumePlugin.setArtifactId(artifactId);
    }

    /**
     * Test that comparison to an artifact representing the same plugin matches.
     */
    @Test
    public void testMatches() {
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getClassifier()).thenReturn(classifier);
        when(artifact.getType()).thenReturn(type);
        assertThat(flumePlugin.matches(artifact)).isTrue();
    }

    /**
     * If the artifact ID differs, then the two should not match.
     */
    @Test
    public void testMatchesDifferentArtifactId() {
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(StringUtils.reverse(artifactId));
        when(artifact.getClassifier()).thenReturn(classifier);
        when(artifact.getType()).thenReturn(type);
        assertThat(flumePlugin.matches(artifact)).isFalse();
    }

    /**
     * If the classifier differs, then the two should not match.
     */
    @Test
    public void testMatchesDifferentClassifier() {
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getClassifier()).thenReturn(StringUtils.reverse(classifier));
        when(artifact.getType()).thenReturn(type);
        assertThat(flumePlugin.matches(artifact)).isFalse();
    }

    /**
     * If the group ID differs, then the two should not match.
     */
    @Test
    public void testMatchesDifferentGroupId() {
        when(artifact.getGroupId()).thenReturn(StringUtils.reverse(groupId));
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getClassifier()).thenReturn(classifier);
        when(artifact.getType()).thenReturn(type);
        assertThat(flumePlugin.matches(artifact)).isFalse();
    }

    /**
     * If the type differs, then the two should not match.
     */
    @Test
    public void testMatchesDifferentType() {
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getClassifier()).thenReturn(classifier);
        when(artifact.getType()).thenReturn(StringUtils.reverse(type));
        assertThat(flumePlugin.matches(artifact)).isFalse();
    }
}
