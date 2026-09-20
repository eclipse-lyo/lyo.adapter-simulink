package clients;

import util.FileMetadata;

/**
 * Stub of the single Subversion file synchronizer.
 *
 * <p>The real implementation depends on the external {@code svnkit-client}
 * module, which is not bundled. This stub instead serves a single sample file
 * metadata entry loaded from a configurable location (see
 * {@link SvnSampleData}), so the Simulink adapter runs standalone without a
 * Subversion server.</p>
 */
@Deprecated
public class SubversionFileClient {
	public FileMetadata syncFile(String svnurl, String localDir, String user, String password) {
		return new SubversionServiceStandaloneImpl().syncFile(svnurl, localDir, user, password);
	}
}
