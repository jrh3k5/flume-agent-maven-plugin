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

/**
 * A container used to share known state about an agent process across mojo invocations.
 * 
 * @author Joshua Hyde
 */

public class AgentProcessContainer {
    private static AgentProcess agentProcess;

    /**
     * Stop any previously-stored, started agent process.
     */
    public static void stopAgentProcess() {
        if (agentProcess != null) {
            agentProcess.stop();
            agentProcess = null;
        }
    }

    /**
     * Store an agent process.
     * 
     * @param agentProcess
     *            The {@link AgentProcess} to be stored for later interaction and retrieval.
     */
    public static void storeAgentProcess(AgentProcess agentProcess) {
        AgentProcessContainer.agentProcess = agentProcess;
    }
}
