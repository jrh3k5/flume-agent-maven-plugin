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
package com.github.jrh3k5.mojo.flume.process;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * A representation of a Flume agent process.
 * 
 * @author Joshua Hyde
 */

public class AgentProcess {
    private final Map<AgentArguments, String> arguments;
    private final File flumeDirectory;
    private Process process;

    /**
     * Create a new builder for an agent process.
     * 
     * @param flumeDirectory
     *            A {@link File} representing the location in which Flume is installed.
     * @return A builder.
     */
    public static Builder newBuilder(File flumeDirectory) {
        return new Builder(flumeDirectory);
    }

    /**
     * Create an agent process.
     * 
     * @param flumeDirectory
     *            A {@link File} representing the location of the Flume installation.
     * @param arguments
     *            A {@link Map} of the arguments to be used during the invocation of the Flume process.
     */
    private AgentProcess(File flumeDirectory, Map<AgentArguments, String> arguments) {
        this.flumeDirectory = flumeDirectory;
        this.arguments = Collections.unmodifiableMap(arguments);
    }

    /**
     * Get the name of the agent.
     * 
     * @return The name of the agent.
     * @since 1.2
     */
    public String getAgentName() {
        final String agentName = arguments.get(AgentArguments.AGENT_NAME);
        if (agentName == null) {
            throw new IllegalStateException("No agent name found in given arguments.");
        }
        return agentName;
    }

    /**
     * Join this thread to the execution of the Flume agent.
     * 
     * @throws InterruptedException
     *             If anything interrupts the joining.
     */
    public void join() throws InterruptedException {
        if (process == null) {
            return;
        }
        process.waitFor();
    }

    /**
     * Start the process.
     * 
     * @throws IllegalStateException
     *             If the Flume agent has already been started and has not been {@link #stop() stopped} since being started.
     * @throws IOException
     *             If any errors occur while attempting to start the Flume agent.
     */
    public void start() throws IOException {
        if (process != null) {
            throw new IllegalStateException("An agent process is already being managed by this object and another cannot be started.");
        }

        process = new ProcessBuilderProxy(flumeDirectory, getProcessArgs(flumeDirectory)).start();

        // Register a shutdown hook to ensure that the process is terminated with the JVM
        Runtime.getRuntime().addShutdownHook(new Thread(new FlumeShutdownRunnable(this), getClass().getCanonicalName() + "-shutdown-thread-" + UUID.randomUUID().toString()));
    }

    /**
     * Stop the Flume agent process.
     */
    void stop() {
        if (process != null) {
            process.destroy();
            process = null;
        }
    }

    /**
     * This proxy class wraps the work of a {@link ProcessBuilder} and its return values. This is to facilitate testing - hence, why it is also package-private in scope.
     * 
     * @author Joshua Hyde
     */
    static class ProcessBuilderProxy {
        private final File flumeDirectory;
        private final List<String> processArgs;

        /**
         * Create a builder proxy.
         * 
         * @param flumeDirectory
         *            A {@link File} representing the location of the Flume installation.
         * @param processArgs
         *            A {@link List} representing the arguments to be used to invoke Flume.
         */
        ProcessBuilderProxy(File flumeDirectory, List<String> processArgs) {
            this.flumeDirectory = flumeDirectory;
            this.processArgs = processArgs;
        }

        /**
         * Create a new builder to start a Flume agent process.
         * 
         * @return A {@link ProcessBuilder} that can be used to start a Flume agent process.
         */
        ProcessBuilder newProcessBuilder() {
            final ProcessBuilder builder = new ProcessBuilder(processArgs);
            builder.redirectErrorStream(true);
            builder.directory(flumeDirectory);
            return builder;
        }

        /**
         * Start the Flume process.
         * 
         * @return A {@link Process} representing the started Flume process.
         * @throws IOException
         *             If any errors occur while trying to invoke the Flume process.
         */
        Process start() throws IOException {
            return newProcessBuilder().start();
        }
    }

    /**
     * Get the process arguments to be used when invoking the Flume agent.
     * <p />
     * This is intentionally made package-private for testing purposes.
     *
     * @param flumeDirectory A {@link File} describing the directory in which Flume is installed.
     * @return A {@link List} of {@link String} objects representing the process arguments to be used.
     */
    private List<String> getProcessArgs(File flumeDirectory) {
        final List<String> processArgs = new ArrayList<>((arguments.size() * 2) + 2);
        if(SystemUtils.IS_OS_WINDOWS) {
            processArgs.add(new File(flumeDirectory, "bin/flume-ng.cmd").getAbsolutePath());
        } else {
            processArgs.add("bin/flume-ng");
        }
        processArgs.add("agent");
        for (Entry<AgentArguments, String> argument : arguments.entrySet()) {
            processArgs.add(String.format("-%s", argument.getKey().getArgumentName()));
            processArgs.add(argument.getValue());
        }
        return processArgs;
    }

    /**
     * A builder used to construct {@link AgentProcess} objects.
     * 
     * @author Joshua Hyde
     */
    public static class Builder {
        private final Map<AgentArguments, String> arguments = new EnumMap<>(AgentArguments.class);
        private final File flumeDirectory;

        /**
         * Create a builder.
         * 
         * @param flumeDirectory
         *            A {@link File} representing the directory of the Flume installation.
         */
        private Builder(File flumeDirectory) {
            this.arguments.put(AgentArguments.CONFIGURATION_DIRECTORY, "conf");
            this.flumeDirectory = flumeDirectory;
        }

        /**
         * Build the agent process.
         * 
         * @return An {@link AgentProcess} representing a Flume agent process ready to be started.
         * @throws IllegalStateException
         *             If any required parameters have not yet been set.
         */
        public AgentProcess build() {
            for (AgentArguments value : AgentArguments.values()) {
                if (value.isRequired() && !arguments.containsKey(value)) {
                    throw new IllegalStateException("Required parameter missing: " + value);
                }
            }

            return new AgentProcess(flumeDirectory, arguments);
        }

        /**
         * Set the name of the agent to be started.
         * 
         * @param agentName
         *            The name of the agent to be started.
         * @return This builder.
         */
        public AgentProcess.Builder withAgent(String agentName) {
            arguments.put(AgentArguments.AGENT_NAME, agentName);
            return this;
        }

        /**
         * Set the location of the configuration file that is to be used to inform the Flume agent how to run.
         * 
         * @param configFile
         *            A {@link File} object representing the location of the Flume agent configuration file.
         * @return This builder.
         */
        public AgentProcess.Builder withConfigFile(File configFile) {
            arguments.put(AgentArguments.CONFIGURATION_FILE, configFile.getAbsolutePath());
            return this;
        }
    }

    /**
     * Enumerations of the possible agent arguments.
     * 
     * @author Joshua Hyde
     */
    private enum AgentArguments {
        /**
         * The name of the agent.
         */
        AGENT_NAME("n"),
        /**
         * The location of the configuration directory for the Flume installation as a whole.
         */
        CONFIGURATION_DIRECTORY("c"),
        /**
         * The location of the file used to inform the behavior of the Flume agent.
         */
        CONFIGURATION_FILE("f");

        private final String argumentName;
        private final boolean required;

        /**
         * Create a required argument.
         * 
         * @param argumentName
         *            The name of the argument as it is used on the command line.
         */
        AgentArguments(String argumentName) {
            this(argumentName, true);
        }

        /**
         * Create an argument.
         * 
         * @param argumentName
         *            The name of the argument as it is used on the command line.
         * @param required
         *            A {@code boolean} field that determines whether or not the field is required.
         */
        AgentArguments(String argumentName, boolean required) {
            this.argumentName = argumentName;
            this.required = required;
        }

        /**
         * Get the name of the argument as it is used on the command line.
         * 
         * @return The name of the argument as it is used on the command line.
         */
        public String getArgumentName() {
            return argumentName;
        }

        /**
         * Determine whether or not the argument is required.
         * 
         * @return {@code true} if the argument is required; {@code false} if the argument is optional.
         */
        public boolean isRequired() {
            return required;
        }
    }

    /**
     * A {@link Runnable} to be used in shutting down the Flume process.
     * 
     * @author Joshua Hyde
     */
    private static class FlumeShutdownRunnable implements Runnable {
        private AgentProcess agentProcess;
    
        /**
         * Create a shutdown task.
         * 
         * @param agentProcess
         *            The {@link AgentProcess} to be stopped.
         */
        FlumeShutdownRunnable(AgentProcess agentProcess) {
            this.agentProcess = agentProcess;
        }
    
        @Override
        public void run() {
            agentProcess.stop();
        }
    }
}
