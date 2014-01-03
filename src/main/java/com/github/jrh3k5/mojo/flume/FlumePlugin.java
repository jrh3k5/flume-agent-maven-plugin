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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.maven.artifact.Artifact;

/**
 * Definition of a Flume plugin that's to be used with the installation of Flume.
 * 
 * @author Joshua Hyde
 */

public class FlumePlugin {
    private String groupId;
    private String artifactId;
    private String classifier = "flume-plugin";
    private String type = "tar.gz";

    /**
     * Get the artifact ID of the Flume plugin.
     * 
     * @return The artifact ID of the Flume plugin.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Get the classifier to be matched.
     * 
     * @return The classifier to be matched.
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * Get the group ID of the Flume plugin.
     * 
     * @return The group ID of the Flume plugin.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Get the type to be matched.
     * 
     * @return The type to be matched.
     */
    public String getType() {
        return type;
    }

    /**
     * Determine whether or not the given artifact represents this Flume plugin.
     * 
     * @param artifact
     *            The {@link Artifact} to be matched.
     * @return {@code true} if the given artifact is judged to be a representation of this Flume plugin; {@code false} if not.
     */
    public boolean matches(Artifact artifact) {
        boolean matches = getGroupId().equals(artifact.getGroupId());
        matches &= getArtifactId().equals(artifact.getArtifactId());
        matches &= classifier.matches(artifact.getClassifier());
        matches &= type.equals(artifact.getType());
        return matches;
    }

    /**
     * Set the artifact ID of the Flume plugin.
     * 
     * @param artifactId
     *            The artifact ID of the Flume plugin.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Set the classifier to be matched.
     * 
     * @param classifier
     *            The classifier to be matched.
     */
    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    /**
     * Set the group ID of the Flume plugin.
     * 
     * @param groupId
     *            The group ID of the Flume plugin.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Set the type to be matched.
     * 
     * @param type
     *            The type to be matched.
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
