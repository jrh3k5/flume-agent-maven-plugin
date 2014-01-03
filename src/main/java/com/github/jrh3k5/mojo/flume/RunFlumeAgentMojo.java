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

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.github.jrh3k5.mojo.flume.process.AgentProcess;

/**
 * A mojo used to run a Flume agent standalone.
 * 
 * @author Joshua Hyde
 */

@Mojo(name = "run-agent", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class RunFlumeAgentMojo extends AbstractFlumeAgentMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final AgentProcess agentProcess = buildAgentProcess();
        try {
            agentProcess.start();
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to start Flume agent.", e);
        }
        getLog().info(String.format("Agent %s running.", getAgentName()));
        try {
            agentProcess.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
