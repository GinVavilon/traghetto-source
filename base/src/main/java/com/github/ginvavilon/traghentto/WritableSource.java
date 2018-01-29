/**
 *
 */
package com.github.ginvavilon.traghentto;

import java.io.IOException;
import java.io.OutputStream;

import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public interface WritableSource extends DeletableSource {

    boolean create() throws IOException;
    boolean createConteiner() throws IOException;
    WritableSource getChild(String name);

    StreamResource<OutputStream> openOutputResource(StreamParams pParams) throws IOException, IOSourceException;

    StreamResource<OutputStream> openOutputResource() throws IOException, IOSourceException;

}
