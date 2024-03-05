package com.github.ginvavilon.traghentto;

import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.VoidParams;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class IOSourceUtils {

    public static byte[] readResource(StreamResource<? extends InputStream> resource, int initialSize) {

        BufferedInputStream in = null;

        try {
            InputStream inputStream = resource.getStream();
            in = new BufferedInputStream(inputStream, StreamUtils.IO_BUFFER_SIZE);
            int size = Math.max(in.available(), initialSize);
            byte[] result = new byte[size];
            int index = 0;
            byte[] buffer = new byte[StreamUtils.BUFFER_SIZE];
            int count;
            while ((count = in.read(buffer)) != -1) {
                if (result.length < index + count) {
                    result = Arrays.copyOf(result, index + count);
                }
                System.arraycopy(buffer, 0, result, index, count);
                index += count;
            }
            return result;
        } catch (final IOException e) {
            Logger.e(e);
            return null;
        } finally {
            StreamUtils.close(in);
            StreamUtils.close(resource);
        }

    }

    public static String readStringFromResource(StreamResource<? extends InputStream> resource) {

        BufferedInputStream in = null;

        try {
            InputStream inputStream = resource.getStream();
            in = new BufferedInputStream(inputStream, StreamUtils.IO_BUFFER_SIZE);
            StringBuilder builder = new StringBuilder();

            byte[] buffer = new byte[StreamUtils.BUFFER_SIZE];
            int count;
            while ((count = in.read(buffer)) != -1) {
                builder.append(new String(buffer, 0, count));
            }
            return builder.toString();
        } catch (final IOException e) {
            Logger.e(e);
            return null;
        } finally {
            StreamUtils.close(in);
            StreamUtils.close(resource);
        }

    }

    public static String readStringFromSource(Source source) throws IOException {
        StreamResource<? extends InputStream> in;
        try {
            in = source.openResource(new VoidParams());

        } catch (IOSourceException e) {
            IOException exception = new IOException(e.getMessage());
            exception.setStackTrace(e.getStackTrace());
            throw exception;
        }
        if (in == null) {
            throw new IOException("not opened stream");
        }
        String string = readStringFromResource(in);
        if (string == null) {
            throw new IOException("not read string");
        }
        return string;
    }

    public static byte[] readSource(Source source) throws IOException {
        StreamResource<? extends InputStream> in;
        try {
            in = source.openResource(new VoidParams());

        } catch (IOSourceException e) {
            IOException exception = new IOException(e.getMessage());
            exception.setStackTrace(e.getStackTrace());
            throw exception;
        }
        if (in == null) {
            throw new IOException("not opened stream");
        }
        byte[] result = readResource(in, (int) source.getLength());
        if (result == null) {
            throw new IOException("not read source");
        }
        return result;
    }

    public static boolean writeSource(WritableSource pSource, String pData) {
        StreamResource<OutputStream> resource = null;

        try {
            pSource.create();
            if (!pSource.exists()) {
                return false;
            }
            resource = pSource.openOutputResource();
            OutputStream stream = resource.getStream();
            stream.write(pData.getBytes());
            return true;
        } catch (IOException | IOSourceException e) {
            Logger.e(e);
        } finally {
            StreamUtils.close(resource);
        }

        return false;
    }

    public static boolean writeSource(WritableSource pSource, byte[] pData) {
        StreamResource<OutputStream> resource = null;

        try {
            pSource.create();
            if (!pSource.exists()) {
                return false;
            }
            resource = pSource.openOutputResource();
            OutputStream stream = resource.getStream();
            stream.write(pData);
            return true;
        } catch (IOException | IOSourceException e) {
            Logger.e(e);
        } finally {
            StreamUtils.close(resource);
        }

        return false;
    }

}
