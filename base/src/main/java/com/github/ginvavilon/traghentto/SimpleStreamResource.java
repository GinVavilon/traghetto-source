/**
 * 
 */
package com.github.ginvavilon.traghentto;

import java.io.Closeable;
import java.io.IOException;

final class SimpleStreamResource<T extends Closeable> implements StreamResource<T> {
    private final T mStream;

    SimpleStreamResource(T inputStream) {
        mStream = inputStream;
    }

    @Override
    public void close() throws IOException {
        mStream.close();
    }

    @Override
    public T getStream() {
        return mStream;
    }

    @Override
    public T takeStream() throws UnsupportedOperationException {
        return mStream;
    }
}