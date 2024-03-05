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

    long UNKNOWN_LENGTH = -1;

    List<? extends Source> getChildren();
    Source getChild(String name);
    boolean isContainer();

    StreamResource<InputStream> openResource(StreamParams pParams)
            throws IOSourceException, IOException;
    String getPath();
    String getName();

    String getUriString();
    boolean exists();
    long getLength();
    boolean isLocal();
    boolean isDataAvailable();

    SourceIterator iterator();

}
