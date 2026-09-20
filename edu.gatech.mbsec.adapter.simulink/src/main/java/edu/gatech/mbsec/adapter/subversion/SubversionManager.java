package edu.gatech.mbsec.adapter.subversion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import clients.SubversionService;
import util.FileMetadata;

/**
 * Converts backend-neutral Subversion metadata into OSLC resources.
 */
public class SubversionManager {

	private static final Logger LOG = LoggerFactory.getLogger(SubversionManager.class);

	static Map<String, SubversionManager> subversionManagerMap = new HashMap<String, SubversionManager>();

	private static SubversionManager instance;

	private Map<String, SubversionFile> qNameOslcSubversionFileMap = new HashMap<String, SubversionFile>();
	private String baseHTTPURI;
	private final SubversionService subversionService;

	public SubversionManager(String baseHTTPURI) {
		this(baseHTTPURI, null);
	}

	public SubversionManager(String baseHTTPURI, SubversionService subversionService) {
		this.baseHTTPURI = baseHTTPURI;
		this.subversionService = subversionService;
		instance = this;
		if (baseHTTPURI.contains("magicdraw")) {
			subversionManagerMap.put("magicdraw", this);
		} else if (baseHTTPURI.contains("simulink")) {
			subversionManagerMap.put("simulink", this);
		} else if (baseHTTPURI.contains("amesim")) {
			subversionManagerMap.put("amesim", this);
		}
	}

	public static SubversionManager getSubversionManager(String baseURIString) {
		if (baseURIString != null) {
			if (baseURIString.contains("magicdraw")) {
				return subversionManagerMap.get("magicdraw");
			} else if (baseURIString.contains("simulink")) {
				return subversionManagerMap.get("simulink");
			} else if (baseURIString.contains("amesim")) {
				return subversionManagerMap.get("amesim");
			}
		}
		return instance;
	}

	public Collection<SubversionFile> getSubversionFiles() {
		if (qNameOslcSubversionFileMap.isEmpty() && subversionService != null) {
			try {
				convertFileMetaDataIntoRDFSubversionFileResources(
						new ArrayList<>(subversionService.syncWorkingCopy(null, null, null, null)));
			} catch (Exception exception) {
				throw new IllegalStateException("Could not load Subversion metadata", exception);
			}
		}
		return qNameOslcSubversionFileMap.values();
	}

	public SubversionFile getFileByURI(String string) {
		return qNameOslcSubversionFileMap.get(string);
	}

	public void convertFileMetaDataIntoRDFSubversionFileResources(ArrayList<FileMetadata> fileMetaDatas) {
		if (fileMetaDatas == null) {
			return;
		}
		for (FileMetadata fm : fileMetaDatas) {
			if (fm == null) {
				continue;
			}
			try {
				final SubversionFile sf = new SubversionFile();
				final String key = fm.getSvnURL();
				if (key != null && !key.isEmpty()) {
					sf.setAbout(new URI(key));
				}
				sf.setName(fm.getPath());
				sf.setPath(fm.getPath());
				sf.setAuthor(fm.getAuthor());
				sf.setCommittedDate(fm.getCommittedDate());
				sf.setRepositoryRootURL(fm.getRepositoryRootURL());
				sf.setRevision(fm.getRevision());
				sf.setSvnURL(fm.getSvnURL());
				qNameOslcSubversionFileMap.put(key, sf);
			} catch (final URISyntaxException e) {
				// skip entries whose svnURL is not a valid URI
				LOG.trace(
						"Skipping Subversion metadata with an invalid svnURL", e);
			}
		}
	}
}
