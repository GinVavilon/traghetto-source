/**
 *
 */
package com.github.ginvavilon.traghentto;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.github.ginvavilon.traghentto.Logger.Level;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.VoidParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class StreamUtils {

    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static final int BUFFER_SIZE = 1024;

    public static boolean close(Closeable pCloseable) {
        if (pCloseable != null) {
            try {
                pCloseable.close();
                return true;
            } catch (IOException e) {
                Logger.e(Level.STREAM, e);
            }
        }
        return false;
    }

    public static boolean closeInSource(ISource pSource, Closeable pCloseable) {
        if (pCloseable != null) {
            try {
                pSource.closeStream(pCloseable);
                return true;
            } catch (IOException e) {
                Logger.e(Level.STREAM, e);
            }
        }
        return false;
    }

    /**
     * Download a bitmap from a URL and write the content to an output stream.
     *
     * @param urlString
     *            The URL to fetch
     * @param inputStream
     * @return true if successful, false otherwise
     */
    public static String readStream(InputStream inputStream) {

        BufferedInputStream in = null;

        try {

            in = new BufferedInputStream(inputStream, IO_BUFFER_SIZE);
            StringBuilder builder = new StringBuilder();

            byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            while ((count = in.read(buffer)) != -1) {
                builder.append(new String(buffer, 0, count));
            }
            return builder.toString();
        } catch (final IOException e) {
            Logger.e(e);
            return null;
        } finally {
            close(in);

        }

    }

    public static long copyStream(InputStream inputStream, OutputStream outputStream,
            ICopyListener pListener) {
        return copyStream(inputStream, outputStream, true, true, 0, 0, pListener);
    }

    public static long copyStream(InputStream inputStream, OutputStream outputStream,
            boolean pAutoClose, ICopyListener pListener) {
        return copyStream(inputStream, outputStream, pAutoClose, pAutoClose, 0, 0, pListener);
    }

    public static long copyStream(InputStream inputStream, OutputStream outputStream,
            boolean pInAutoClose, boolean pOutAutoClose, ICopyListener pListener) {
        return copyStream(inputStream, outputStream, pInAutoClose, pOutAutoClose, 0, 0, pListener);
    }

    public static long copyStream(InputStream inputStream, OutputStream outputStream,
            boolean pInAutoClose, boolean pOutAutoClose, long pAlreadyReaded, long pSkip,
            ICopyListener pListener) {
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        boolean hasListener = pListener != null;
        try {
            if (hasListener) {
                pListener.onStart();
            }
            long readed = pAlreadyReaded;
            if (pSkip > 0) {
                Logger.d(Level.STREAM, "Readed %s", readed);
                long skiped = 0;
                long canSkip = pSkip;
                while ((skiped = inputStream.skip(canSkip)) > 0) {
                    canSkip -= skiped;
                    readed += skiped;
                    if (hasListener) {
                        pListener.onProgress(readed);
                    }
                }
                Logger.d(Level.STREAM, "Skipped %s", readed);
            }

            in = new BufferedInputStream(inputStream, IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
            byte[] buffer = new byte[BUFFER_SIZE];
            int count;

            while ((!Thread.interrupted()) && (count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
                // Log.i("Out :%s", new String(buffer, 0, count));

                out.flush();
                readed += count;
                if (hasListener) {
                    pListener.onProgress(readed);
                }
            }
            if (Thread.interrupted()) {
                if (hasListener) {
                    pListener.onFail(new Exception());
                }
                return -1;
            }
            if (hasListener) {
                pListener.onCompite();
            }

            return readed;
        } catch (final IOException e) {
            Logger.e(e);
            if (hasListener) {
                pListener.onFail(e);
            }
            return -1;
        } finally {
            if (pInAutoClose) {
                close(in);
            }
            if (pOutAutoClose) {
                close(out);
            }
        }

    }

    public static interface ICopyListener {
        void onStart();

        void onProgress(long pRadedByte);

        void onCompite();

        void onFail(Throwable pE);
    }

    public static String readSource(ISource pJsonSource) throws IOException {
        InputStream in;
        try {
            in = pJsonSource.openInputStream(new VoidParams());
        } catch (IOSourceException e) {
            IOException exception = new IOException(e.getMessage());
            exception.setStackTrace(e.getStackTrace());
            throw exception;
        }
        if (in == null) {
            throw new IOException("not opened stream");
        }
        String string = readStream(in);
        if (string == null) {
            throw new IOException("not read string");
        }
        return string;
    }

    public static boolean writeSource(IWritableSource pSource, String pData) {
        OutputStream stream = null;
        try {
            pSource.create();
            if (!pSource.exists()) {
                return false;
            }
            stream = pSource.openOutputStream();
            stream.write(pData.getBytes());
            return true;
        } catch (IOException e) {
            Logger.e(e);
        } finally {
            closeInSource(pSource, stream);
        }

        return false;

    }

}
