/**
 * 
 */
package com.github.ginvavilon.traghentto;

import java.io.IOException;
import java.io.OutputStream;

import com.github.ginvavilon.traghentto.params.StreamParams;
import com.github.ginvavilon.traghentto.params.VoidParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public abstract class BaseWritableSource extends BaseSource implements WritableSource {

    @Override
    public StreamResource<OutputStream> openOutputResource(StreamParams pParams)
            throws IOException {
        return StreamResource.createResource(openOutputStream(pParams));
    }

    @Override
    public StreamResource<OutputStream> openOutputResource() throws IOException {
        return openOutputResource(new VoidParams());
    }

    protected abstract OutputStream openOutputStream(StreamParams pParams) throws IOException;
}
