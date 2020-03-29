/**
 * 
 */
package com.github.ginvavilon.traghentto;

import java.io.Closeable;

/**
 * @author Vladimir Baraznovsky
 *
 */
public interface StreamResource<T extends Closeable> extends Closeable {

    T getStream();

    /**
     * Take stream for controlling by external code
     */
    T takeStream() throws UnsupportedOperationException;

    boolean isOpened();
}
