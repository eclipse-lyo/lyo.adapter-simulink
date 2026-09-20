package clients;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusClient;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import util.FileMetadata;

/** Real SVNKit implementation, compiled and packaged only by {@code -Pfull}. */
public final class SubversionServiceSvnkitImpl implements SubversionService {
    @Override
    public List<FileMetadata> syncWorkingCopy(final String svnurl, final String localDir,
            final String user, final String password) throws Exception {
        requireDirectory(localDir);
        final File directory = new File(localDir);
        try (Client client = new Client(user, password)) {
            if (svnurl != null && !svnurl.isBlank() && !new File(directory, ".svn").exists()) {
                client.updateClient.doCheckout(SVNURL.parseURIEncoded(svnurl), directory,
                        SVNRevision.UNDEFINED, SVNRevision.HEAD, SVNDepth.INFINITY, true);
            } else {
                client.updateClient.doUpdate(directory, SVNRevision.HEAD, SVNDepth.INFINITY, true, true);
            }
            return client.status(directory);
        }
    }

    @Override
    public FileMetadata syncFile(final String svnurl, final String localDir,
            final String user, final String password) throws Exception {
        if (svnurl == null || svnurl.isBlank()) {
            throw new IllegalArgumentException("svnurl is required for an SVN file");
        }
        requireDirectory(localDir);
        final File target = new File(localDir, svnurl.substring(svnurl.lastIndexOf('/') + 1));
        try (Client client = new Client(user, password)) {
            client.updateClient.doCheckout(SVNURL.parseURIEncoded(svnurl), target,
                    SVNRevision.UNDEFINED, SVNRevision.HEAD, SVNDepth.EMPTY, true);
            final List<FileMetadata> files = client.status(target);
            return files.isEmpty() ? null : files.get(0);
        }
    }

    private static void requireDirectory(final String localDir) {
        if (localDir == null || localDir.isBlank()) {
            throw new IllegalArgumentException("localDir is required for a Subversion operation");
        }
        new File(localDir).mkdirs();
    }

    private static final class Client implements AutoCloseable {
        private final SVNClientManager manager;
        private final SVNUpdateClient updateClient;

        Client(final String user, final String password) {
            final ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
            final ISVNAuthenticationManager authentication =
                    SVNWCUtil.createDefaultAuthenticationManager(user, password);
            manager = SVNClientManager.newInstance(options, authentication);
            updateClient = manager.getUpdateClient();
        }

        List<FileMetadata> status(final File path) throws SVNException {
            final List<FileMetadata> files = new ArrayList<>();
            final SVNStatusClient statusClient = manager.getStatusClient();
            statusClient.doStatus(path, SVNRevision.HEAD, true, true, true, false, false,
                    new ISVNStatusHandler() {
                        @Override
                        public void handleStatus(final SVNStatus status) {
                            if (status.getNodeStatus() == null
                                    || status.getKind() != SVNNodeKind.FILE
                                    || status.getURL() == null) {
                                return;
                            }
                            final FileMetadata metadata = new FileMetadata();
                            metadata.setPath(status.getFile() == null ? "" : status.getFile().getPath());
                            metadata.setAuthor(status.getAuthor());
                            metadata.setCommittedDate(status.getCommittedDate() == null
                                    ? null : status.getCommittedDate().toInstant().toString());
                            metadata.setRepositoryRootURL(status.getRepositoryRootURL() == null
                                    ? null : status.getRepositoryRootURL().toString());
                            metadata.setRevision(status.getRevision() == null
                                    ? null : String.valueOf(status.getRevision().getNumber()));
                            metadata.setSvnURL(status.getURL().toString());
                            files.add(metadata);
                        }
                    });
            return files;
        }

        @Override
        public void close() throws IOException {
            manager.dispose();
        }
    }
}
