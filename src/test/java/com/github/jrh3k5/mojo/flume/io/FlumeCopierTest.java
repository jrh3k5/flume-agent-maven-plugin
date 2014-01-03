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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.io.File;
import java.net.URL;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.jrh3k5.mojo.flume.AbstractUnitTest;

/**
 * Unit tests for {@link FlumeCopier}.
 * 
 * @author Joshua Hyde
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ArchiveUtils.class })
public class FlumeCopierTest extends AbstractUnitTest {
    /**
     * Test the copying of the Flume archive.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testCopyTo() throws Exception {
        final File directory = createTestDirectory();
        final URL flumeTarGz = new File(directory, UUID.randomUUID().toString() + ".tar.gz").toURI().toURL();
        final FlumeArchiveCache archiveCache = mock(FlumeArchiveCache.class);
        when(archiveCache.getArchiveLocation()).thenReturn(flumeTarGz);
        when(archiveCache.getFlumeVersion()).thenReturn("1.2.3");

        final FlumeCopier copier = new FlumeCopier(archiveCache);

        mockStatic(ArchiveUtils.class);
        assertThat(copier.copyTo(directory)).isEqualTo(new File(directory, "apache-flume-1.2.3-bin"));

        final File flumeTar = new File(directory, "apache-flume.tar");

        verifyStatic();
        ArchiveUtils.gunzipFile(flumeTarGz, flumeTar);
        ArchiveUtils.untarFile(flumeTar, directory);
    }
}
