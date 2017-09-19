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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A container used to share known state about an agent process across mojo invocations.
 * 
 * @author Joshua Hyde
 */

public class AgentProcessContainer {
    private static final Map<String, AgentProcess> AGENT_PROCESSES = new HashMap<>();
    private static final ReadWriteLock PROCESSES_LOCK = new ReentrantReadWriteLock();

    /**
     * Stop any previously-stored, started agent process.
     * 
     * @param agentName
     *            The name of the agent to be stopped.
     * @since 1.2
     */
    public static void stopAgentProcess(String agentName) {
        final Lock writeLock = PROCESSES_LOCK.writeLock();
        writeLock.lock();
        try {
            final AgentProcess agentProcess = AGENT_PROCESSES.remove(agentName);
            if (agentProcess != null) {
                agentProcess.stop();
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Store an agent process.
     * 
     * @param agentProcess
     *            The {@link AgentProcess} to be stored for later interaction and retrieval.
     */
    public static void storeAgentProcess(AgentProcess agentProcess) {
        final String agentName = agentProcess.getAgentName();
        final Lock writeLock = PROCESSES_LOCK.writeLock();
        writeLock.lock();
        try {
            if(AGENT_PROCESSES.containsKey(agentName)) {
                throw new IllegalStateException(String.format("An agent process by the name %s has already been started. This will require manual cleanup of your Flume processes.", agentProcess));
            }
            AGENT_PROCESSES.put(agentName, agentProcess);
        } finally {
            writeLock.unlock();
        }
    }
}
