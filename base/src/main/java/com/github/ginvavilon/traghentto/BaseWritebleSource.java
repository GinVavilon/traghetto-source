/**
 * 
 */
package com.github.ginvavilon.traghentto;

import java.io.IOException;
import java.io.OutputStream;

import com.github.ginvavilon.traghentto.params.StreamParams;
import com.github.ginvavilon.traghentto.params.VoidParams;

/**
 * @author vbaraznovsky
 *
 */
public abstract class BaseWritebleSource extends BaseSource implements WritableSource {

    @Override
    public StreamResource<OutputStream> openOutputResource(StreamParams pParams)
            throws IOException {
        return StreamUtils.createResource(openOutputStream(pParams));
    }

    @Override
    public StreamResource<OutputStream> openOutputResource() throws IOException {
        return openOutputResource(new VoidParams());
    }

    public abstract OutputStream openOutputStream(StreamParams pParams) throws IOException;
}
