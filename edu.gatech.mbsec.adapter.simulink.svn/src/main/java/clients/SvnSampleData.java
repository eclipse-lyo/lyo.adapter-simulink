package clients;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.FileMetadata;

/**
 * Loads sample Subversion working-copy metadata for the stub backend.
 *
 * <p>The data is read from a configurable location: a file on disk addressed by
 * the {@code svn.sample.files} system property / environment variable, falling
 * back to the classpath resource {@code svn-sample-files.properties}. This keeps
 * the (otherwise external-system-dependent) SVN backend functional when no real
 * Subversion server is available, so the Simulink adapter runs standalone.</p>
 */
public final class SvnSampleData {

    private static final Logger LOG = LoggerFactory.getLogger(SvnSampleData.class);

    private SvnSampleData() {
    }

    public static List<FileMetadata> loadWorkingCopy() {
        final String location = getConfigurationProperty("svn.sample.files", "svn-sample-files.properties");
        final Properties props = loadProperties(location);
        final int count = Integer.parseInt(props.getProperty("svn.file.count", "0"));
        final List<FileMetadata> files = new ArrayList<FileMetadata>();
        for (int i = 0; i < count; i++) {
            final FileMetadata fm = new FileMetadata();
            fm.setPath(props.getProperty("svn.file." + i + ".path", ""));
            fm.setAuthor(props.getProperty("svn.file." + i + ".author", ""));
            fm.setCommittedDate(props.getProperty("svn.file." + i + ".committedDate", ""));
            fm.setRepositoryRootURL(props.getProperty("svn.file." + i + ".repositoryRootURL", ""));
            fm.setRevision(props.getProperty("svn.file." + i + ".revision", ""));
            fm.setSvnURL(props.getProperty("svn.file." + i + ".svnURL", ""));
            files.add(fm);
        }
        LOG.info("Loaded {} sample SVN file metadata entry(ies) from {}", files.size(), location);
        return files;
    }

    public static FileMetadata loadFile() {
        final List<FileMetadata> all = loadWorkingCopy();
        return all.isEmpty() ? null : all.get(0);
    }

    private static Properties loadProperties(final String location) {
        final Properties props = new Properties();
        try (InputStream in = new FileInputStream(location)) {
            props.load(in);
            return props;
        } catch (final IOException fileException) {
            LOG.trace("Could not load sample SVN properties from file {}", location, fileException);
            try (InputStream in = SvnSampleData.class.getResourceAsStream("/" + location)) {
                if (in == null) {
                    LOG.warn("Sample SVN properties not found at '{}' (neither as a file nor as a classpath resource); serving empty working copy",
                            location);
                    return props;
                }
                props.load(in);
                return props;
            } catch (final IOException classpathException) {
                LOG.error("Could not load sample SVN properties from {}", location, classpathException);
                LOG.warn("Could not load sample SVN properties from {}; serving empty working copy", location);
                return props;
            }
        }
    }

    /**
     * Resolves a configuration property using the fallback chain
     * system property -&gt; environment variable -&gt; default value.
     */
    public static String getConfigurationProperty(final String key, final String defaultValue) {
        final String value = System.getProperty(key);
        if (value != null) {
            return value;
        }
        final String env = System.getenv(key);
        return env != null ? env : defaultValue;
    }
}
