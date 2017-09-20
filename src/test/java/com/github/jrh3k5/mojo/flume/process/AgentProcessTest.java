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

import com.github.jrh3k5.mojo.flume.AbstractUnitTest;
import com.github.jrh3k5.mojo.flume.process.AgentProcess.ProcessBuilderProxy;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Unit tests for {@link AgentProcess}.
 * 
 * @author Joshua Hyde
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AgentProcess.class, ProcessBuilderProxy.class })
public class AgentProcessTest extends AbstractUnitTest {
    private final String agentName = UUID.randomUUID().toString();
    private AgentProcess agentProcess;
    private File flumeDirectory;
    private File configFile;

    /**
     * Set up properties for each test.
     * 
     * @throws Exception
     *             If any errors occur during the setup.
     */
    @Before
    public void setUp() throws Exception {
        final File testDirectory = createTestDirectory();
        flumeDirectory = new File(testDirectory, "flume");
        configFile = new File(testDirectory, "flume.properties");
        agentProcess = AgentProcess.newBuilder(flumeDirectory).withAgent(agentName).withConfigFile(configFile).build();
    }

    /**
     * Test the construction of the Flume process.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testBuildFlumeProcess() throws Exception {
        final List<String> args = Arrays.asList("arg1", "arg2");
        final AgentProcess.ProcessBuilderProxy builderProxy = new AgentProcess.ProcessBuilderProxy(flumeDirectory, args);
        final ProcessBuilder builder = builderProxy.newProcessBuilder();
        assertThat(builder.command()).isEqualTo(args);
        assertThat(builder.directory()).isEqualTo(flumeDirectory);
        assertThat(builder.redirectErrorStream()).isTrue();
    }

    /**
     * Test joining to a Flume agent process.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testJoin() throws Exception {
        final Process process = mockAgentStart();
        agentProcess.join();
        verify(process).waitFor();
    }

    /**
     * Test the starting and stopping of the process.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testStart() throws Exception {
        @SuppressWarnings("rawtypes")
        final ArgumentCaptor<List> argsCaptor = ArgumentCaptor.forClass(List.class);
        mockAgentStart(argsCaptor);

        @SuppressWarnings("unchecked")
        final List<String> args = argsCaptor.getValue();
        final String expectedCommand;
        if(SystemUtils.IS_OS_WINDOWS) {
            expectedCommand = new File(flumeDirectory, "bin/flume-ng.cmd").getAbsolutePath();
        } else {
            expectedCommand = "bin/flume-ng";
        }
        // First two commands needed to start the agent - the rest can be in any orders
        assertThat(args.subList(0, 2)).containsExactly(expectedCommand, "agent");
        assertThat(getArguments(args.subList(2, args.size()))).containsOnly(new Argument("-c", "conf"), new Argument("-f", configFile.getAbsolutePath()), new Argument("-n", agentName));
    }

    /**
     * Test the stopping of the agent process.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testStop() throws Exception {
        final Process process = mockAgentStart();
        agentProcess.stop();
        verify(process).destroy();
    }

    /**
     * Start a mock agent process.
     * 
     * @return A mock of a {@link Process} object representing the mocked process.
     * @throws Exception
     *             If any errors occur during the mocking.
     */
    private Process mockAgentStart() throws Exception {
        return mockAgentStart(ArgumentCaptor.forClass(List.class));
    }

    /**
     * Start a mock agent process.
     * 
     * @param argsCaptor
     *            An {@link ArgumentCaptor} used to capture the arguments used to invoke the mocked process startup.
     * @return A mock of a {@link Process} object representing the mocked process.
     * @throws Exception
     *             If any errors occur during the mocking.
     */
    private Process mockAgentStart(@SuppressWarnings("rawtypes") ArgumentCaptor<List> argsCaptor) throws Exception {
        final ProcessBuilderProxy builderProxy = mock(ProcessBuilderProxy.class);
        whenNew(ProcessBuilderProxy.class).withArguments(eq(flumeDirectory), argsCaptor.capture()).thenReturn(builderProxy);

        final Process flumeProcess = mock(Process.class);
        when(builderProxy.start()).thenReturn(flumeProcess);
        agentProcess.start();
        verify(builderProxy).start();
        return flumeProcess;
    }

    private Collection<Argument> getArguments(List<String> args) {
        if (args.size() % 2 != 0) {
            throw new IllegalArgumentException("Unexpected, non-even size of arguments: " + args);
        }

        final Collection<Argument> arguments = new ArrayList<>(args.size() / 2);
        for (int i = 0; i < args.size(); i += 2) {
            arguments.add(new Argument(args.get(i), args.get(i+1)));
        }
        return arguments;
    }

    /**
     * A simple "holder" class for holding the arguments given to an invocation of the Flume agent.
     * 
     * @author jh016266
     * @since 4.0
     */
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    private static class Argument {
        @Getter
        private String argumentName;
        @Getter
        private String argumentValue;
    }
}
