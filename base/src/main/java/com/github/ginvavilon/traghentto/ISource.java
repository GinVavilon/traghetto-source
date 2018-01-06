/**
 *
 */
package com.github.ginvavilon.traghentto;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.ISourceStreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public interface ISource{

    public static final long UNKNOWN_LENGHT=-1;

    List<? extends ISource> getChildren();
    ISource getChild(String name);
    boolean isConteiner();

    InputStream openInputStream(ISourceStreamParams pParams) throws IOException, IOSourceException;
    void closeStream(Closeable pStream) throws IOException;
    String getPath();
    String getName();

    String getUriString();
    boolean exists();
    long getLenght();
    boolean isLocal();
    boolean isDataAvailable();
}
