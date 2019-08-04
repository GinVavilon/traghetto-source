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

class ReadResourceThread extends Thread {

    private StreamResource<InputStream> mResource;
    private OutputStream mOutputStream;

    public ReadResourceThread(StreamResource<InputStream> resource, OutputStream outputStream) {
        mResource = resource;
        mOutputStream = outputStream;
        setDaemon(true);

    }

    @Override
    public void run() {
        try (StreamResource<InputStream> resource = mResource;
                InputStream inputStream = resource.getStream();
                OutputStream outputStream = mOutputStream) {
            StreamUtils.copyStream(inputStream, outputStream, null);
            
        } catch (IOException e) {
            Logger.e(e);
        }
    }

}