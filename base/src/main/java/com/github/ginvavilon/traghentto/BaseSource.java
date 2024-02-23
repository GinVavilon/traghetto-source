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
public abstract class BaseSource implements Source {


    public BaseSource() {

    }

    protected abstract InputStream openInputStream(StreamParams pParams)
            throws IOException, IOSourceException;

    @Override
    public StreamResource<InputStream> openResource(StreamParams pParams)
            throws IOSourceException, IOException {
        StreamParams params = StreamParams.getSafetyParams(pParams);
        InputStream inputStream = openInputStream(params);
        return StreamResource.createResource(inputStream);
    }

    @Override
    public SourceIterator iterator() {
        return new RecursiveSourceIterator(this);
    }

}
