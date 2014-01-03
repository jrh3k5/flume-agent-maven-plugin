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

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * Common helper methods for testing.
 * 
 * @author Joshua Hyde
 */

public abstract class AbstractUnitTest {
    /**
     * A {@link Rule} used to get the current test name.
     */
    @Rule
    public TestName testName = new TestName();

    private final File targetDir = new File("target");

    /**
     * Ensure that the {@code target} directory exists.
     * 
     * @throws Exception
     *             If any errors occur during the directory creation.
     */
    @Before
    public void makeTarget() throws Exception {
        FileUtils.forceMkdir(targetDir);
    }

    /**
     * Create a directory for the current test.
     * 
     * @return A {@link File} representing the possible location of a test directory.
     */
    protected File createTestDirectory() {
        final File testClassDir = new File(targetDir, getClass().getSimpleName());
        return new File(testClassDir, getTestName());
    }

    /**
     * Get the current test's name.
     * 
     * @return The current test's name.
     */
    protected String getTestName() {
        return testName.getMethodName();
    }
}
