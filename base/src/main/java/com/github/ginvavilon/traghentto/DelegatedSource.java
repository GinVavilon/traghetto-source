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

    private final SourceProvider<T> mSourceProvider;

    protected DelegatedSource(SourceProvider<T> sourceProvider) {
        mSourceProvider = sourceProvider;
    }
    
    protected DelegatedSource(T source) {
        this(createSourceProvider(source));
    }

    public List<? extends Source> getChildren() {
        return mSourceProvider.getSource().getChildren();
    }

    public Source getChild(String name) {
        return mSourceProvider.getSource().getChild(name);
    }

    public boolean isConteiner() {
        return mSourceProvider.getSource().isConteiner();
    }

    public StreamResource<InputStream> openResource(StreamParams pParams)
            throws IOSourceException, IOException {
        return mSourceProvider.getSource().openResource(pParams);
    }

    public String getPath() {
        return mSourceProvider.getSource().getPath();
    }

    public String getName() {
        return mSourceProvider.getSource().getName();
    }

    public String getUriString() {
        return mSourceProvider.getSource().getUriString();
    }

    public boolean exists() {
        return mSourceProvider.getSource().exists();
    }

    public long getLenght() {
        return mSourceProvider.getSource().getLenght();
    }

    public boolean isLocal() {
        return mSourceProvider.getSource().isLocal();
    }

    public boolean isDataAvailable() {
        return mSourceProvider.getSource().isDataAvailable();
    }

    protected final T getSource(){
        return mSourceProvider.getSource();
    }

    @Override
    public SourceIterator iterator() {
        return mSourceProvider.getSource().iterator();
    }

    private static <T>SourceProvider<T> createSourceProvider(T source) {
        return new SourceProvider<T>() {
            @Override
            public T getSource() {
                return source;
            }
        };
    }


    public interface SourceProvider<T> {
        T getSource();
    }
}
