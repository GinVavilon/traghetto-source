/**
 * 
 */
package com.github.ginvavilon.traghentto;

import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Vladimir Baraznovsky
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

    public boolean isContainer() {
        return mSourceProvider.getSource().isContainer();
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

    public long getLength() {
        return mSourceProvider.getSource().getLength();
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

    protected static <T> SourceProvider<T> createSourceProvider(T source) {
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

    protected static <T extends Closeable> StreamResource<T> wrapStreamResource(
            StreamResource<T> resource,
            StreamProcessor<T> processor) throws IOException {

        T sourceStream = resource.getStream();
        try {
            processor.process(sourceStream);
            return resource;
        } catch (IOException e) {
            try {
                resource.close();
            } catch (IOException newE) {
                e.addSuppressed(newE);
            }
            throw e;
        }

    }

    protected interface StreamProcessor<T extends Closeable> {
        void process(T stream) throws IOException;
    }

}
