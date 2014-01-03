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
package com.github.jrh3k5.mojo.flume.io;

import org.codehaus.plexus.logging.AbstractLogger;
import org.slf4j.Logger;

/**
 * A bridge between Plexus logging and slf4j logging.
 * 
 * @author Joshua Hyde
 */

class Slf4jPlexusLogger extends AbstractLogger {
    private final org.slf4j.Logger logger;

    /**
     * Determine a {@link org.codehaus.plexus.logging.Logger} level based on the configuration of the given {@link Logger}.
     * 
     * @param log
     *            The {@link Logger} to determine the logger level.
     * @return A {@link org.codehaus.plexus.logging.Logger} level corresponding to the log level in the given {@link Logger}.
     */
    private static int determineThreshold(Logger log) {
        if (log.isDebugEnabled()) {
            return org.codehaus.plexus.logging.Logger.LEVEL_DEBUG;
        } else if (log.isInfoEnabled()) {
            return org.codehaus.plexus.logging.Logger.LEVEL_INFO;
        } else if (log.isWarnEnabled()) {
            return org.codehaus.plexus.logging.Logger.LEVEL_WARN;
        } else if (log.isErrorEnabled()) {
            return org.codehaus.plexus.logging.Logger.LEVEL_ERROR;
        } else {
            return org.codehaus.plexus.logging.Logger.LEVEL_DISABLED;
        }
    }

    /**
     * Create a logger.
     * 
     * @param owner
     *            The {@link Class} that owns the logger.
     */
    Slf4jPlexusLogger(Class<?> owner) {
        this(owner, org.slf4j.LoggerFactory.getLogger(owner));
    }

    /**
     * Create a logger.
     * 
     * @param owner
     *            The {@link Class} that owns the logger.
     * @param logger
     *            The {@link Logger} that backs this object.
     */
    private Slf4jPlexusLogger(Class<?> owner, org.slf4j.Logger logger) {
        super(determineThreshold(logger), owner.getCanonicalName());
        this.logger = logger;
    }

    @Override
    public void debug(String message, Throwable throwable) {
        logger.debug(message, throwable);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    @Override
    public void fatalError(String message, Throwable throwable) {
        error(message, throwable);
    }

    @Override
    public org.codehaus.plexus.logging.Logger getChildLogger(String name) {
        return this;
    }

    @Override
    public void info(String message, Throwable throwable) {
        logger.info(message, throwable);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

}
