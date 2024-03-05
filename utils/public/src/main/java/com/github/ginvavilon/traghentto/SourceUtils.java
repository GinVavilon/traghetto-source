/**
 *
 */
package com.github.ginvavilon.traghentto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.github.ginvavilon.traghentto.StreamUtils.ICopyListener;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.exceptions.RenameException;
import com.github.ginvavilon.traghentto.exceptions.SourceAlreadyExistsException;
import com.github.ginvavilon.traghentto.params.ParamNames;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class SourceUtils {

    public static void copy(Source pFrom, WritableSource pTo)
            throws IOException, IOSourceException, SourceAlreadyExistsException {
        copy(pFrom, pTo, null);
    }

    public static void replace(Source pFrom, WritableSource pTo)
            throws IOException, IOSourceException, SourceAlreadyExistsException {
        replace(pFrom, pTo, null);
    }

    public static void copy(Source pFrom, WritableSource pTo, ICopyListener pListener)
            throws IOException, IOSourceException, SourceAlreadyExistsException {
        copy(pFrom, pTo, false, null, null, pListener);
    }

    public static void replace(Source pFrom, WritableSource pTo, ICopyListener pListener)
            throws IOException, IOSourceException {
        try {
            copy(pFrom, pTo, true, null, null, pListener);
        } catch (SourceAlreadyExistsException e) {
            Logger.e(e);
        }
    }

    public static void copy(Source pFrom, WritableSource pTo, boolean update,
            StreamParams pInParams,
            StreamParams pOutParams, ICopyListener pListener)
            throws IOException, IOSourceException, SourceAlreadyExistsException {
        if (pFrom.isContainer()) {
            pTo.createContainer();

            List<? extends Source> children = pFrom.getChildren();
            
            for (Source child : children) {
                WritableSource toChild = pTo.getChild(child.getName());
                copy(child, toChild, update, null, null, null);
            }
            
        } else {

            if ((update) || (!pTo.exists())) {
                WritableSource to = pTo;
                if (to.isContainer()) {
                    to = to.getChild(pFrom.getName());
                }

                to.create();

                InputStream inputStream = null;
                StreamResource<InputStream> inputResource = null;
                StreamResource<OutputStream> outputResource = null;
                OutputStream outputStream = null;
                try {
                    StreamParams inParam = StreamParams.getSafetyParams(pInParams);
                    inputResource = pFrom.openResource(inParam);
                    outputResource = to.openOutputResource(pOutParams);
                    inputStream = inputResource.getStream();
                    outputStream = outputResource.getStream();
                    long skipByte = inParam.getProperty(ParamNames.SKIP, 0L);
                    skipByte = inParam.getProperty(ParamNames.OUT_SKIP, skipByte);
                    long alreadyRead = inParam.getProperty(ParamNames.OUT_ALREADY_READ, 0L);
                    StreamUtils.copyStream(inputStream, outputStream, false, false, alreadyRead,
                            skipByte, delegateCopyListener(pListener));

                } finally {
                    StreamUtils.close(inputResource);
                    StreamUtils.close(outputResource);
                }
            } else {
                throw new SourceAlreadyExistsException();
            }

        }
    }

    private static StreamUtils.ICopyListener delegateCopyListener(ICopyListener pListener) {
        if (pListener != null) {
            return new DelegateCopyListener(pListener);
        }
        return null;
    }


    public static RenamedSource rename(RenamedSource source, String newName)
            throws RenameException {
        RenamedSource newSource = source.createRenamedSource(newName);
        source.rename(newSource);
        return newSource;
    }

    public static boolean isChild(Source parentSource, Source childSource) {
        if (parentSource == null) {
            return false;
        }

        if (childSource == null) {
            return false;
        }
        return childSource.getUriString().startsWith(parentSource.getUriString());
    }


    public static interface ICopyListener {
        void onStart();

        void onProgress(long pReadBytes);

        void onComplete();

        void onFail(Throwable pE);
    }

    private static class DelegateCopyListener implements StreamUtils.ICopyListener {
        private final ICopyListener mPListener;

        public DelegateCopyListener(ICopyListener pListener) {
            mPListener = pListener;
        }

        @Override
        public void onStart() {
            mPListener.onStart();
        }

        @Override
        public void onProgress(long pReadBytes) {
            mPListener.onProgress(pReadBytes);
        }

        @Override
        public void onComplete() {
            mPListener.onComplete();
        }

        @Override
        public void onFail(Throwable pE) {
            mPListener.onFail(pE);
        }
    }
}

