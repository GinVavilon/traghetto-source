/**
 * 
 */
package com.github.ginvavilon.traghentto.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceIterator;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.StreamSource;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author vbaraznovsky
 *
 */
public class NotExistZipEntrySource extends ZipEntrySource implements StreamSource {

    private String mPath;


    public NotExistZipEntrySource(ZipSource pZipParrent, String path) {
        super(pZipParrent);
        mPath = path;
    }

    @Override
    public List<? extends Source> getChildren() {
        return null;
    }

    @Override
    public Source getChild(String name) {
        return new NotExistZipEntrySource(mZipParrent, mPath + "/" + name);
    }

    @Override
    public boolean isConteiner() {
        return false;
    }

    @Override
    public StreamResource<InputStream> openResource(StreamParams pParams)
            throws IOSourceException, IOException {
        throw new IOException("Entry don't exits");
    }

    @Override
    public String getPath() {
        return mPath;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public long getLenght() {
        return UNKNOWN_LENGHT;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isDataAvailable() {
        return false;
    }

    @Override
    public void close() throws IOException {
        throw new IOException("Entry don't exits");
    }

    @Override
    public void open() throws IOException {
        throw new IOException("Entry don't exits");
    }

    @Override
    public boolean isOpened() {
        return false;
    }

    @Override
    public SourceIterator iterator() {
        return null;
    }

}
