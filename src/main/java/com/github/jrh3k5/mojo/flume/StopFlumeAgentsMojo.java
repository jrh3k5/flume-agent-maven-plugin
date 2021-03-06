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

import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.github.jrh3k5.mojo.flume.process.AgentProcessContainer;

/**
 * A mojo used to stop Flume agents.
 * 
 * @author Joshua Hyde
 * @since 2.0
 */

@Mojo(name = "stop", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class StopFlumeAgentsMojo extends AbstractMojo {
    /**
     * The configuration of agents to be run, expressed as {@link Agent} objects.
     */
    @Parameter(required = true)
    private List<Agent> agents = Collections.emptyList();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        for (Agent agent : agents) {
            AgentProcessContainer.stopAgentProcess(agent.getAgentName());
        }
    }
}
