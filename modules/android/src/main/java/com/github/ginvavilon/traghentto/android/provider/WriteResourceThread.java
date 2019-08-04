/**
 * 
 */
package com.github.ginvavilon.traghentto.android.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.StreamUtils;

class WriteResourceThread extends Thread {

    private StreamResource<OutputStream> mResource;
    private InputStream mInputStream;

    public WriteResourceThread(StreamResource<OutputStream> resource, InputStream inputStream) {
        mResource = resource;
        mInputStream = inputStream;

        setDaemon(true);

    }

    @Override
    public void run() {
        try (StreamResource<OutputStream> resource = mResource;
                OutputStream outputStream = resource.getStream();
                InputStream inputStream = mInputStream) {
            StreamUtils.copyStream(inputStream, outputStream, null);

        } catch (IOException e) {
            Logger.e(e);
        }
    }

}