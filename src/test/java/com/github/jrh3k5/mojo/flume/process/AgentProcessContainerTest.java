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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Test;

/**
 * Unit tests for {@link AgentProcessContainer}.
 * 
 * @author Joshua Hyde
 * @since 1.2
 */

public class AgentProcessContainerTest {
    /**
     * Test the stopping of a stored agent process.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testStopAgentProcess() throws Exception {
        final String agentName = UUID.randomUUID().toString();
        final AgentProcess agentProcess = mock(AgentProcess.class);
        when(agentProcess.getAgentName()).thenReturn(agentName);

        AgentProcessContainer.storeAgentProcess(agentProcess);
        AgentProcessContainer.stopAgentProcess(agentName);
        verify(agentProcess).stop();

        // Calling stop on a previously-stopped agent should not call stop on it again
        AgentProcessContainer.stopAgentProcess(agentName);
        verify(agentProcess).stop();
    }

    /**
     * If the agent process has already been stored in the container, then an {@link IllegalStateException} should be thrown.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testStoreAgentProcessDuplicate() throws Exception {
        final String agentName = UUID.randomUUID().toString();
        final AgentProcess agentProcess = mock(AgentProcess.class);
        when(agentProcess.getAgentName()).thenReturn(agentName);

        AgentProcessContainer.storeAgentProcess(agentProcess);

        IllegalStateException caught = null;
        try {
            AgentProcessContainer.storeAgentProcess(agentProcess);
        } catch (IllegalStateException e) {
            caught = e;
        }
        assertThat(caught).isNotNull();
    }
}
