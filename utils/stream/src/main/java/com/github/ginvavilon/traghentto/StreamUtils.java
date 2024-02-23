/**
 *
 */
package com.github.ginvavilon.traghentto;

import com.github.ginvavilon.traghentto.Logger.Level;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class StreamUtils {

    static final int IO_BUFFER_SIZE = 8 * 1024;
    static final int BUFFER_SIZE = 1024;

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
            boolean pInAutoClose, boolean pOutAutoClose, long pAlreadyRead, long pSkip,
            ICopyListener pListener) {
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        boolean hasListener = pListener != null;
        try {
            if (hasListener) {
                pListener.onStart();
            }
            long alreadyRead = pAlreadyRead;
            if (pSkip > 0) {
                Logger.d(Level.STREAM, "Read %s", alreadyRead);
                long skipped = 0;
                long canSkip = pSkip;
                while ((skipped = inputStream.skip(canSkip)) > 0) {
                    canSkip -= skipped;
                    alreadyRead += skipped;
                    if (hasListener) {
                        pListener.onProgress(alreadyRead);
                    }
                }
                Logger.d(Level.STREAM, "Skipped %s", alreadyRead);
            }

            in = new BufferedInputStream(inputStream, IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
            byte[] buffer = new byte[BUFFER_SIZE];
            int count;

            while ((!Thread.interrupted()) && (count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
                // Log.i("Out :%s", new String(buffer, 0, count));

                out.flush();
                alreadyRead += count;
                if (hasListener) {
                    pListener.onProgress(alreadyRead);
                }
            }
            if (Thread.interrupted()) {
                if (hasListener) {
                    pListener.onFail(new Exception());
                }
                return -1;
            }
            if (hasListener) {
                pListener.onComplete();
            }
            outputStream.flush();
            return alreadyRead;
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

        void onProgress(long pReadBytes);

        void onComplete();

        void onFail(Throwable pE);
    }

}
