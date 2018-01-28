/**
 * 
 */
package com.github.ginvavilon.traghentto;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author vbaraznovsky
 *
 */
public abstract class DelegatedSource<T extends Source> implements Source {

    protected final T mSource;

    public DelegatedSource(T source) {
        super();
        mSource = source;
    }

    public List<? extends Source> getChildren() {
        return mSource.getChildren();
    }

    public Source getChild(String name) {
        return mSource.getChild(name);
    }

    public boolean isConteiner() {
        return mSource.isConteiner();
    }

    public StreamResource<InputStream> openResource(StreamParams pParams)
            throws IOSourceException, IOException {
        return mSource.openResource(pParams);
    }

    public String getPath() {
        return mSource.getPath();
    }

    public String getName() {
        return mSource.getName();
    }

    public String getUriString() {
        return mSource.getUriString();
    }

    public boolean exists() {
        return mSource.exists();
    }

    public long getLenght() {
        return mSource.getLenght();
    }

    public boolean isLocal() {
        return mSource.isLocal();
    }

    public boolean isDataAvailable() {
        return mSource.isDataAvailable();
    }

}
