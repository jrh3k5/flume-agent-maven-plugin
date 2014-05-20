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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.util.Collections;
import java.util.UUID;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.github.jrh3k5.mojo.flume.process.AgentProcess;
import com.github.jrh3k5.mojo.flume.process.AgentProcessContainer;

/**
 * Unit tests for {@link StartFlumeAgentsMojo}.
 * 
 * @author Joshua Hyde
 * @since 2.0
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AgentProcessContainer.class, StartFlumeAgentsMojo.class })
public class StartFlumeAgentsMojoTest {
    /**
     * Test the starting of a Flume agent.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testExecuteMojo() throws Exception {
        mockStatic(AgentProcessContainer.class);

        final String agentName = UUID.randomUUID().toString();
        final Agent agent = mock(Agent.class);
        final AgentProcess agentProcess = mock(AgentProcess.class);
        // TODO: make sure that the correct Agent instance was passed in
        final StartFlumeAgentsMojo toTest = new StartFlumeAgentsMojo() {
            @Override
            protected Agent getAgent(String givenAgentName) {
                assertThat(givenAgentName).isEqualTo(agentName);
                return agent;
            }

            @Override
            protected AgentProcess buildAgentProcess(Agent givenAgent) throws MojoExecutionException {
                assertThat(givenAgent).isEqualTo(agent);
                return agentProcess;
            }
        };
        Whitebox.setInternalState(toTest, "agentNames", Collections.singletonList(agentName));
        toTest.execute();
        verify(agentProcess).start();
        verifyStatic();
        AgentProcessContainer.storeAgentProcess(agentProcess);
    }
}
