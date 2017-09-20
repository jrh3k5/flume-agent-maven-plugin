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

/**
 * Definition of a class representing any changes to be made to the Flume agent's {@code lib/} directory.
 * 
 * @author Joshua Hyde
 * @since 1.1
 */

public class Libs {
    private List<String> removals = Collections.emptyList();

    /**
     * Get the list of libraries, if any, that are to be removed.
     *
     * @return A {@link List} of {@link String} representing the libraries to be removed.
     */
    List<String> getRemovals() {
        return removals;
    }

    /**
     * Set the list of libraries to be removed.
     *
     * @param removals
     *            A {@link List} of {@link String} representing the libraries to be removed.
     */
    void setRemovals(List<String> removals) {
        this.removals = Collections.unmodifiableList(removals);
    }
}
