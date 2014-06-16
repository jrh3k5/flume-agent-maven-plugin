package com.github.jrh3k5.mojo.flume.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

/**
 * An object used to retrieve and cache the Flume binary archive.
 * 
 * @author Joshua Hyde
 */

public class FlumeArchiveCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlumeArchiveCache.class);
    private static final Pattern BIN_NAME_PATTERN = Pattern.compile("(.*)(apache-flume-([\\d]+\\.[\\d]+\\.[\\d]+)-bin\\.tar\\.gz)");
    private static final int PATTERN_FILENAME_INDEX = 2;
    private static final int PATTERN_VERSION_INDEX = 3;

    private final URL archiveUrl;
    private final String md5Hash;
    private final String fileName;
    private final String flumeVersion;

    /**
     * Create a Flume archive cache.
     * 
     * @param archiveUrl
     *            A {@link URL} from which the archive will be downloaded (if not previously cached).
     * @param md5Hash
     *            The MD5 hash to be used to verify the integrity of the download archive.
     */
    public FlumeArchiveCache(URL archiveUrl, String md5Hash) {
        final Matcher matcher = BIN_NAME_PATTERN.matcher(archiveUrl.toExternalForm());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("The given archive URL is an unexpected format: " + archiveUrl.toExternalForm());
        }
        this.fileName = matcher.group(PATTERN_FILENAME_INDEX);
        this.flumeVersion = matcher.group(PATTERN_VERSION_INDEX);
        this.archiveUrl = archiveUrl;
        this.md5Hash = md5Hash;
    }

    /**
     * Get the location of the archive (post-caching).
     * 
     * @return A {@link URL} from which the archive can be retrieved.
     * @throws IOException
     *             If any errors occur while trying to resolve the URL.
     */
    public URL getArchiveLocation() throws IOException {
        final File tempDir = FileUtils.getTempDirectory();
        final File flumeCache = new File(tempDir, fileName);
        LOGGER.info("The Flume archive is being cached in {}.", flumeCache.getAbsolutePath());
        if (flumeCache.exists()) {
            final String fileHash = Files.hash(flumeCache, Hashing.md5()).toString();
            if (!fileHash.equals(md5Hash)) {
                LOGGER.warn("The local Flume archive copy has a hash of {}, but expected a hash of {}. It will be downloaded again.", fileHash, md5Hash);
                FileUtils.forceDelete(flumeCache);
            } else {
                return flumeCache.toURI().toURL();
            }
        }
        
        final InputStream urlIn = archiveUrl.openStream();
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(flumeCache);
            IOUtils.copy(urlIn, fileOut);
        } finally {
            IOUtils.closeQuietly(fileOut);
            IOUtils.closeQuietly(urlIn);
        }

        return flumeCache.toURI().toURL();
    }

    /**
     * Get the version of Flume in use.
     * 
     * @return The version of Flume in use.
     */
    public String getFlumeVersion() {
        return flumeVersion;
    }
}
