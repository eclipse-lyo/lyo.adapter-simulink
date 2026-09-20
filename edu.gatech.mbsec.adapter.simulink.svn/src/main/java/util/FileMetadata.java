package util;

/**
 * NO-OP STUB of the Subversion file-metadata DTO, shaded locally so the
 * Simulink adapter compiles without the external {@code svnkit-client} module.
 */
public class FileMetadata {
	private String path;
	private String author;
	private String committedDate;
	private String repositoryRootURL;
	private String revision;
	private String svnURL;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getCommittedDate() {
		return committedDate;
	}

	public void setCommittedDate(String committedDate) {
		this.committedDate = committedDate;
	}

	public String getRepositoryRootURL() {
		return repositoryRootURL;
	}

	public void setRepositoryRootURL(String repositoryRootURL) {
		this.repositoryRootURL = repositoryRootURL;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getSvnURL() {
		return svnURL;
	}

	public void setSvnURL(String svnURL) {
		this.svnURL = svnURL;
	}
}
