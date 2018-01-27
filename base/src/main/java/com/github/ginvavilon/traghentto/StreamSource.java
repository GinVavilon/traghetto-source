/**
 *
 */
package com.github.ginvavilon.traghentto;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Vladimir Baraznovsky
 *
 */
public interface StreamSource extends Source ,Closeable{
    void open() throws IOException;
    boolean isOpened();

}
