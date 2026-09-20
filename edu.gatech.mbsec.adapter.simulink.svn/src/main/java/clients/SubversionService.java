package clients;

import java.util.List;

import util.FileMetadata;

/** Backend-neutral seam for Subversion operations. */
public interface SubversionService {
    List<FileMetadata> syncWorkingCopy(String svnurl, String localDir,
            String user, String password) throws Exception;

    FileMetadata syncFile(String svnurl, String localDir,
            String user, String password) throws Exception;
}
