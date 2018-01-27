/**
 *
 */
package com.github.ginvavilon.traghentto;

import java.io.IOException;
import java.io.OutputStream;

import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public interface WritableSource extends DeletableSource {
    OutputStream openOutputStream() throws IOException;
    boolean create() throws IOException;
    boolean createConteiner() throws IOException;
    WritableSource getChild(String name);
    OutputStream openOutputStream(StreamParams pParams) throws IOException;
}
