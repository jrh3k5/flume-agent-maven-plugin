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

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * A representation of an agent to be executed.
 * 
 * @author Joshua Hyde
 * @since 2.0
 */

@Getter @Setter
public class Agent {
    /**
     * The list of any external Flume plugins to be installed into the agent's plugins directory.
     *
     * @param flumePlugins
     *            A {@link List} of {@link FlumePlugin} objects representing any third-party plugins used by the agent.
     * @return A {@link List} of {@link FlumePlugin} objects representing any third-party plugins used by the agent.
     */
    private List<FlumePlugin> flumePlugins = Collections.emptyList();
    /**
     * The name of the agent.
     *
     * @param agentName
     *            The name of the agent.
     * @return The name of the agent.
     */
    private String agentName;
    /**
     * The location of the Flume agent's configuration file.
     *
     * @param configFile
     *            A {@link File} object representing the location of the agent's configuration file.
     * @return A {@link File} object representing the location of the agent's configuration file.
     */
    private File configFile;
    /**
     * The JVM arguments to be passed into the Flume agent as part of its runtime environment configuration.
     *
     * @param javaOpts
     *            The JVM arguments to be supplied to the Flume agent's JVM.
     * @return The JVM arguments to be supplied to the Flume agent's JVM.
     */
    private String javaOpts = "-Xmx20m";
    /**
     * The file containing the logging configuration properties.
     *
     * @param loggingProperties
     *            {@code null} if no logging properties file has been provided and Flume's default configuration should be used; otherwise, a {@link File} representing the location of a file
     *            describing the properties of the Flume agent logging configuration to be used.
     * @return {@code null} if no logging properties file has been provided and Flume's default configuration should be used; otherwise, a {@link File} representing the location of a file describing
     *         the properties of the Flume agent logging configuration to be used.
     * @since 2.1.1
     */
    private File loggingProperties;
    /**
     * The configuration of the Flume agent's {@code libs/} directory.
     *
     * @param libs
     *            A {@link Libs} object representing the desired configuration of the agent's {@code libs/} directory.
     * @return A {@link Libs} object representing the desired configuration of the agent's {@code libs/} directory.
     */
    private Libs libs = new Libs();
}
