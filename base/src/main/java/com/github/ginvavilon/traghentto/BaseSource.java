/**
 * 
 */
package com.github.ginvavilon.traghentto;

import java.io.IOException;
import java.io.InputStream;

import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author vbaraznovsky
 *
 */
public abstract class BaseSource implements Source {

    public BaseSource() {

    }

    public abstract InputStream openInputStream(StreamParams pParams)
            throws IOException, IOSourceException;

    @Override
    public StreamResource<InputStream> openResource(StreamParams pParams)
            throws IOSourceException, IOException {
        InputStream inputStream = openInputStream(pParams);
        return StreamUtils.createResource(inputStream);
    }


}
