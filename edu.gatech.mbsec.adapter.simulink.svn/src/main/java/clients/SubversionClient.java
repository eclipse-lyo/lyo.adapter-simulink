package clients;

import java.util.ArrayList;

import util.FileMetadata;

/**
 * Stub of the Subversion working-copy synchronizer.
 *
 * <p>The real implementation depends on the external {@code svnkit-client}
 * module, which is not bundled. This stub instead serves sample working-copy
 * metadata loaded from a configurable location (see {@link SvnSampleData}), so
 * the Simulink adapter runs standalone without a Subversion server.</p>
 */
@Deprecated
public class SubversionClient {
	public static ArrayList<FileMetadata> syncWorkingCopy(String svnurl, String localDir, String user, String password) {
		return new ArrayList<FileMetadata>(new SubversionServiceStandaloneImpl()
				.syncWorkingCopy(svnurl, localDir, user, password));
	}
}
