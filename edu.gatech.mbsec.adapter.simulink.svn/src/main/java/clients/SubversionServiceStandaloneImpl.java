package clients;

import java.util.ArrayList;
import java.util.List;

import util.FileMetadata;

/** Standalone implementation backed by packaged sample metadata. */
public final class SubversionServiceStandaloneImpl implements SubversionService {
    @Override
    public List<FileMetadata> syncWorkingCopy(final String svnurl, final String localDir,
            final String user, final String password) {
        return new ArrayList<>(SvnSampleData.loadWorkingCopy());
    }

    @Override
    public FileMetadata syncFile(final String svnurl, final String localDir,
            final String user, final String password) {
        return SvnSampleData.loadFile();
    }
}
