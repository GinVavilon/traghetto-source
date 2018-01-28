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
 * @author Vladimir Baraznovsky
 *
 */
public interface Source extends Iterable<Source> {

    public static final long UNKNOWN_LENGHT=-1;

    List<? extends Source> getChildren();
    Source getChild(String name);
    boolean isConteiner();

    StreamResource<InputStream> openResource(StreamParams pParams)
            throws IOSourceException, IOException;
    String getPath();
    String getName();

    String getUriString();
    boolean exists();
    long getLenght();
    boolean isLocal();
    boolean isDataAvailable();

    SourceIterator iterator();

}
