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

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.util.Collections;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.github.jrh3k5.mojo.flume.process.AgentProcessContainer;

/**
 * Unit tests for {@link StopFlumeAgentsMojo}.
 * 
 * @author Joshua Hyde
 * @since 2.0
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AgentProcessContainer.class })
public class StopFlumeAgentsMojoTest {
    private final StopFlumeAgentsMojo mojo = new StopFlumeAgentsMojo();

    /**
     * Test the stopping of an agent.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testExecuteMojo() throws Exception {
        final String agentName = UUID.randomUUID().toString();
        Whitebox.setInternalState(mojo, "agentNames", Collections.singletonList(agentName));

        mockStatic(AgentProcessContainer.class);
        mojo.execute();
        verifyStatic();
        AgentProcessContainer.stopAgentProcess(agentName);
    }
}
