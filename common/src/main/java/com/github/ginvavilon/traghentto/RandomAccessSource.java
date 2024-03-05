/**
 *
 */
package com.github.ginvavilon.traghentto;

import java.io.IOException;
import java.io.InputStream;

import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class RandomAccessSource extends BaseRandomAccessSource<StreamSource> implements Source {

    public RandomAccessSource(StreamSource pSource) {
        super(pSource, pSource);
    }

    @Override
    public StreamResource<InputStream> openResource(StreamParams pParams)
            throws IOSourceException, IOException {
        openStream();
        StreamResource<InputStream> resource = getSource().openResource(pParams);
        return new RandomSourceResource(resource);
    }

    private final class RandomSourceResource implements StreamResource<InputStream> {
        private final StreamResource<InputStream> mResource;

        private RandomSourceResource(StreamResource<InputStream> resource) {
            mResource = resource;
        }

        @Override
        public void close() throws IOException {

            IOException exception = null;
            try {
                mResource.close();
            } catch (IOException e) {
                exception = e;
            } finally {
                try {
                    getSource().close();
                } catch (IOException e) {
                    if (exception != null) {
                        exception.addSuppressed(e);
                    } else {
                        exception = e;
                    }
                }
                if (exception != null) {
                    throw exception;
                }
            }

        }

        @Override
        public InputStream getStream() {
            return mResource.getStream();
        }

        @Override
        public InputStream takeStream() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Stream must be closed by this resource");
        }

        @Override
        public boolean isOpened() {
            return getSource().isOpened() && mResource.isOpened();
        }
    }

    @Override
    public SourceIterator iterator() {
        try {
            openStream();
            SourceIterator iterator = getSource().iterator();
            return new SourceIterator() {

                @Override
                public void close() throws IOException {
                    closeStream();
                }

                @Override
                public Source next() {
                    return iterator.next();
                }

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
