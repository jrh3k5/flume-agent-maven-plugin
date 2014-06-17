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

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * A representation of an agent to be executed.
 * 
 * @author Joshua Hyde
 * @since 2.0
 */

public class Agent {
    private List<FlumePlugin> flumePlugins = Collections.emptyList();
    private String agentName;
    private File configFile;
    private String javaOpts = "-Xmx20m";
    private File loggingProperties;
    private Libs libs = new Libs();

    /**
     * Get the name of the agent.
     * 
     * @return The name of the agent.
     */
    public String getAgentName() {
        return agentName;
    }

    /**
     * Get the location of the Flume agent's configuration file.
     * 
     * @return A {@link File} object representing the location of the agent's configuration file.
     */
    public File getConfigFile() {
        return configFile;
    }

    /**
     * Get the list of any external Flume plugins to be installed into the agent's plugins directory.
     * 
     * @return A {@list List} of {@link FlumePlugin} objects representing any third-party plugins used by the agent.
     */
    public List<FlumePlugin> getFlumePlugins() {
        return flumePlugins;
    }

    /**
     * Get the JVM arguments to be passed into the Flume agent as part of its runtime environment configuration.
     * 
     * @return The JVM arguments to be supplied to the Flume agent's JVM.
     */
    public String getJavaOpts() {
        return javaOpts;
    }

    /**
     * Get the configuration of the Flume agent's {@code libs/} directory.
     * 
     * @return A {@link Libs} object representing the desired configuration of the agent's {@code libs/} directory.
     */
    public Libs getLibs() {
        return libs;
    }

    /**
     * Get the file contining the logging configuration properties.
     * 
     * @return {@code null} if no logging properties file has been provided and Flume's default configuration should be used; otherwise, a {@link File} representing the location of a file describing
     *         the properties of the Flume agent logging configuration to be used.
     * @since 2.1.1
     */
    public File getLoggingProperties() {
        return loggingProperties;
    }

    /**
     * Set the name of the agent.
     * 
     * @param agentName
     *            The name of the agent.
     */
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    /**
     * Set the location of the Flume agent's configuration file.
     * 
     * @param configFile
     *            A {@link File} object representing the location of the agent's configuration file.
     */
    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }

    /**
     * Set the list of any external Flume plugins to be installed into the agent's plugins directory.
     * 
     * @param flumePlugins
     *            A {@list List} of {@link FlumePlugin} objects representing any third-party plugins used by the agent.
     */
    public void setFlumePlugins(List<FlumePlugin> flumePlugins) {
        this.flumePlugins = flumePlugins == null ? Collections.<FlumePlugin> emptyList() : Collections.unmodifiableList(flumePlugins);
    }

    /**
     * Set the JVM arguments to be passed into the Flume agent as part of its runtime environment configuration.
     * 
     * @param javaOpts
     *            The JVM arguments to be supplied to the Flume agent's JVM.
     */
    public void setJavaOpts(String javaOpts) {
        this.javaOpts = javaOpts;
    }

    /**
     * Set the configuration of the Flume agent's {@code libs/} directory.
     * 
     * @param libs
     *            A {@link Libs} object representing the desired configuration of the agent's {@code libs/} directory.
     */
    public void setLibs(Libs libs) {
        this.libs = libs;
    }

    /**
     * Set the file contining the logging configuration properties.
     * 
     * @param loggingProperties
     *            {@code null} if no logging properties file has been provided and Flume's default configuration should be used; otherwise, a {@link File} representing the location of a file
     *            describing the properties of the Flume agent logging configuration to be used.
     * @since 2.1.1
     */
    public void setLoggingProperties(File loggingProperties) {
        this.loggingProperties = loggingProperties;
    }
}
