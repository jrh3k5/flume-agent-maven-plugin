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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.jrh3k5.mojo.flume.process.AgentProcess;
import com.github.jrh3k5.mojo.flume.process.AgentProcessContainer;

/**
 * Unit tests for {@link StartFlumeAgentMojo}.
 * 
 * @author Joshua Hyde
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AgentProcessContainer.class, StartFlumeAgentMojo.class })
public class StartFlumeAgentMojoTest {
    /**
     * Test the starting of a Flume agent.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testExecuteMojo() throws Exception {
        mockStatic(AgentProcessContainer.class);
        final AgentProcess agentProcess = mock(AgentProcess.class);
        final StartFlumeAgentMojo toTest = new StartFlumeAgentMojo() {
            @Override
            protected AgentProcess buildAgentProcess() throws MojoExecutionException {
                return agentProcess;
            }
        };
        toTest.execute();
        verify(agentProcess).start();
        verifyStatic();
        AgentProcessContainer.storeAgentProcess(agentProcess);
    }
}
