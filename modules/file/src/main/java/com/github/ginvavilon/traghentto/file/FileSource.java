/**
 *
 */
package com.github.ginvavilon.traghentto.file;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.ginvavilon.traghentto.ISource;
import com.github.ginvavilon.traghentto.IWritableSource;
import com.github.ginvavilon.traghentto.SourceCreator;
import com.github.ginvavilon.traghentto.SourceUtils;
import com.github.ginvavilon.traghentto.params.ISourceStreamParams;
import com.github.ginvavilon.traghentto.params.ParamNames;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class FileSource implements ISource, IWritableSource {
    private File mFile;

    public FileSource(File pFile) {
        super();
        mFile = pFile;
    }

    @Override
    public List<? extends FileSource> getChildren() {
        List<FileSource> list = new ArrayList<FileSource>();
        File[] listFiles = mFile.listFiles();
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
    public boolean isConteiner() {
        return mFile.isDirectory();
    }

    @Override
    public InputStream openInputStream(ISourceStreamParams pParams) throws IOException {
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
    public void closeStream(Closeable pStream) throws IOException {
        pStream.close();
    }

    @Override
    public String getUriString() {
        return mFile.toURI().toString();
    }

    @Override
    public long getLenght() {
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
    public OutputStream openOutputStream(ISourceStreamParams pParams) throws IOException {
        ISourceStreamParams params = SourceUtils.getSaflyParams(pParams);
        return new FileOutputStream(mFile, params.getProperty(ParamNames.APPEND, false));
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return openOutputStream(null);
    }

    @Override
    public boolean createConteiner() throws IOException {
        return mFile.mkdirs();
    }

    @Override
    public boolean create() throws IOException {
        File parentFile = mFile.getParentFile();
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

}
