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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.archiver.tar.TarUnArchiver;

/**
 * Utilities for managing and interacting with archives.
 * 
 * @author Joshua Hyde
 */

public class ArchiveUtils {
    /**
     * Un-GZIP a file.
     * 
     * @param toUnzip
     *            A {@link URL} representing the GZIP file to be unzipped.
     * @param toFile
     *            A {@link File} representing the location to which the unzipped file should be placed.
     * @throws IOException
     *             If any errors occur during the unzipping.
     * @see #gunzipFile(URL, File)
     */
    public static void gunzipFile(URL toUnzip, File toFile) throws IOException {
        if (toFile.exists() && !toFile.isFile()) {
            throw new IllegalArgumentException("Destination file " + toFile + " exists, but is not a file and, as such, cannot be written to.");
        }

        try (final GZIPInputStream zipIn = new GZIPInputStream(toUnzip.openStream()); final FileOutputStream fileOut = new FileOutputStream(toFile)){
            IOUtils.copy(zipIn, fileOut);
        }
    }

    /**
     * Extract the contents of a TAR file.
     * 
     * @param tarFile
     *            A {@link File} representing the TAR file whose contents are to be extracted.
     * @param toDirectory
     *            A {@link File} representing the directory to which the contents of the TAR file to be extracted.
     * @throws IllegalArgumentException
     *             If the given TAR file is not a file or does not exist or the given output directory is not a directory or does not exist.
     * @throws IOException
     *             If any errors occur during the extraction.
     */
    public static void untarFile(File tarFile, File toDirectory) throws IOException {
        if (!tarFile.isFile()) {
            throw new IllegalArgumentException("TAR file " + tarFile + " must be an existent file.");
        }

        if (!toDirectory.exists()) {
            FileUtils.forceMkdir(toDirectory);
        }

        if (!toDirectory.isDirectory()) {
            throw new IllegalArgumentException("Output directory " + toDirectory + " must be an existent directory.");
        }

        final TarUnArchiver unarchiver = new TarUnArchiver(tarFile);
        unarchiver.enableLogging(new Slf4jPlexusLogger(FlumeCopier.class));
        unarchiver.setDestDirectory(toDirectory);
        unarchiver.extract();
    }
}
