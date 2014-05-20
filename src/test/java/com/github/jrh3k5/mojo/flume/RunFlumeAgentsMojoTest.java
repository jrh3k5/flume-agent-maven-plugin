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

import java.util.Collections;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import com.github.jrh3k5.mojo.flume.process.AgentProcess;

/**
 * Unit tests for {@link RunFlumeAgentsMojo}.
 * 
 * @author Joshua Hyde
 * @since 2.0
 */

public class RunFlumeAgentsMojoTest {
    /**
     * Test the execution of the mojo.
     * 
     * @throws Exception
     *             If any errors occur during the test.
     */
    @Test
    public void testExecuteMojo() throws Exception {
        final Agent agent = mock(Agent.class);
        final AgentProcess agentProcess = mock(AgentProcess.class);

        final RunFlumeAgentsMojo toTest = new RunFlumeAgentsMojo() {
            @Override
            protected AgentProcess buildAgentProcess(Agent givenAgent) throws MojoExecutionException {
                assertThat(givenAgent).isEqualTo(agent);
                return agentProcess;
            }
        };
        Whitebox.setInternalState(toTest, "agents", Collections.singletonList(agent));
        toTest.execute();
        verify(agentProcess).start();
        verify(agentProcess).join();
    }
}
