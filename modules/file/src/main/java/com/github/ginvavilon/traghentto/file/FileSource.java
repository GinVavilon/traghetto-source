/**
 *
 */
package com.github.ginvavilon.traghentto.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.ginvavilon.traghentto.BaseWritableSource;
import com.github.ginvavilon.traghentto.RenamedSource;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceCreator;
import com.github.ginvavilon.traghentto.SourceUtils;
import com.github.ginvavilon.traghentto.WritableSource;
import com.github.ginvavilon.traghentto.exceptions.RenameException;
import com.github.ginvavilon.traghentto.params.ParamNames;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class FileSource extends BaseWritableSource implements Source, WritableSource, RenamedSource {
    private File mFile;

    public FileSource(File pFile) {
        super();
        mFile = pFile;
    }

    @Override
    public List<? extends FileSource> getChildren() {
        List<FileSource> list = new ArrayList<FileSource>();
        File[] listFiles = mFile.listFiles();
        if (listFiles == null) {
            return null;
        }
        for (File file : listFiles) {
            FileSource source = new FileSource(file);
            list.add(source);
        }
        return list;
    }

    @Override
    public FileSource getChild(String pName) {
        File file = new File(mFile, pName);
        return new FileSource(file);
    }

    @Override
    public boolean isContainer() {
        return mFile.isDirectory();
    }

    @Override
    protected InputStream openInputStream(StreamParams pParams) throws IOException {
        return new FileInputStream(mFile);
    }

    @Override
    public String getPath() {
        return mFile.getPath();
    }

    @Override
    public String getName() {
        return mFile.getName();
    }

    @Override
    public boolean exists() {
        return mFile.exists();
    }

    @Override
    public String getUriString() {
        return mFile.toURI().toString();
    }

    @Override
    public long getLength() {
        return mFile.length();
    }

    @Override
    public String toString() {
        return getUriString().toString();
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public boolean isDataAvailable() {
        return true;
    }

    @Override
    protected OutputStream openOutputStream(StreamParams pParams) throws IOException {
        StreamParams params = SourceUtils.getSafetyParams(pParams);
        if (!(params.getProperty(ParamNames.CREATE, true) || exists())) {
            throw new FileNotFoundException();
        }

        return new FileOutputStream(mFile, params.getProperty(ParamNames.APPEND, false));
    }

    @Override
    public boolean createContainer() throws IOException {
        return mFile.mkdirs();
    }

    @Override
    public boolean create() throws IOException {
        File parentFile = mFile.getAbsoluteFile().getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        return mFile.createNewFile();
    }

    public File getFile() {
        return mFile;
    }

    @Override
    public boolean delete() {
        return recursiveDelete(mFile);
    }

    private static boolean recursiveDelete(File pFile) {
        if (pFile.isDirectory()) {
            File[] listFiles = pFile.listFiles();
            if (listFiles ==null) {
            	return false;
            }
            for (File file : listFiles) {
                recursiveDelete(file);
            }
        }
        return pFile.delete();

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mFile == null) ? 0 : mFile.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FileSource other = (FileSource) obj;
        if (mFile == null) {
            if (other.mFile != null)
                return false;
        } else if (!mFile.equals(other.mFile))
            return false;
        return true;
    }

    public static final SourceCreator<FileSource> CREATOR = new SourceCreator<FileSource>() {

        @Override
        public FileSource create(String pParam) {
            return new FileSource(new File(pParam));
        }

    };

    @Override
    public void rename(RenamedSource source) throws RenameException {
        if (source instanceof FileSource) {
            File file = ((FileSource) source).getFile();
            
            boolean result = mFile.renameTo(file);
            if (!result) {
                throw new RenameException("Rename is failed to file " + String.valueOf(file));
            }
            return;
        }
        throw new RenameException("Source must be" + this.getClass().getName());

    }

    @Override
    public RenamedSource createRenamedSource(String name) {
        File parentFile = mFile.getParentFile();
        return new FileSource(new File(parentFile, name));
    }

    @Override
    public boolean canBeRenamed(RenamedSource source) {
        return source instanceof FileSource;
    }

    @Override
    public boolean canBeDeleted() {
        return mFile.canWrite();
    }

    @Override
    public boolean isWritable() {
        return mFile.canWrite();
    }


}
