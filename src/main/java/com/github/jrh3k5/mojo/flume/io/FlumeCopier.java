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

import static com.github.jrh3k5.mojo.flume.io.ArchiveUtils.gunzipFile;
import static com.github.jrh3k5.mojo.flume.io.ArchiveUtils.untarFile;

import java.io.File;
import java.io.IOException;

/**
 * A utility class used to copy a Flume installation.
 * 
 * @author Joshua Hyde
 */

public class FlumeCopier {
    private final FlumeArchiveCache archiveCache;

    /**
     * Create a copier.
     * 
     * @param archiveCache
     *            A {@link FlumeArchiveCache} to control from where the Flume archive will be copied.
     */
    public FlumeCopier(FlumeArchiveCache archiveCache) {
        this.archiveCache = archiveCache;
    }

    /**
     * Copy the Flume installation to a given directory.
     * 
     * @param directory
     *            A {@link File} representing the directory to which Flume is to be installed.
     * @return A {@link File} representing the location of the unpacked Flume installation.
     * @throws IOException
     *             If any errors occur during the unpacking of the Flume installation.
     */
    public File copyTo(File directory) throws IOException {
        final File tarFile = new File(directory, "apache-flume.tar");
        gunzipFile(archiveCache.getArchiveLocation(), tarFile);
        untarFile(tarFile, directory);
        return new File(directory, String.format("apache-flume-%s-bin", archiveCache.getFlumeVersion()));
    }
}
