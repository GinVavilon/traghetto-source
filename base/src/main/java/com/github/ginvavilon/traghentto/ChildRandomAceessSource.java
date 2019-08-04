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
class ChildRandomAceessSource extends BaseRandomAccessSource<Source> implements Source {


    public ChildRandomAceessSource(StreamSource parentSource, Source source) {
        super(parentSource, source);
    }
    

    @Override
    public StreamResource<InputStream> openResource(StreamParams pParams)
            throws IOSourceException, IOException {
        final boolean opened = !isStreamOpened();
        if (opened) {
            openStream();
        }
        final StreamResource<InputStream> resource = super.openResource(pParams);
        return new StreamResource<InputStream>() {

            @Override
            public void close() throws IOException {
                resource.close();
                if (opened) {
                    closeStream();
                }
            }

            @Override
            public InputStream takeStream() throws UnsupportedOperationException {
                throw new UnsupportedOperationException("Stream must be closed by this resource");
            }

            @Override
            public InputStream getStream() {
                return resource.getStream();
            }

            @Override
            public boolean isOpened() {
                return isStreamOpened();
            }
        };
    }


}
