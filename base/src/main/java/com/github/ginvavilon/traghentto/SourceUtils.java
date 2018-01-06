/**
 *
 */
package com.github.ginvavilon.traghentto;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.github.ginvavilon.traghentto.StreamUtils.ICopyListener;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.exceptions.SourceAlreadyExistsException;
import com.github.ginvavilon.traghentto.params.ISourceStreamParams;
import com.github.ginvavilon.traghentto.params.ParamNames;
import com.github.ginvavilon.traghentto.params.VoidParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class SourceUtils {

    public static void copy(ISource pFrom, IWritableSource pTo)
            throws IOException, IOSourceException, SourceAlreadyExistsException {
        copy(pFrom, pTo, null);
    }

    public static void replace(ISource pFrom, IWritableSource pTo)
            throws IOException, IOSourceException, SourceAlreadyExistsException {
        replace(pFrom, pTo, null);
    }

    public static void copy(ISource pFrom, IWritableSource pTo, ICopyListener pListener)
            throws IOException, IOSourceException, SourceAlreadyExistsException {
        copy(pFrom, pTo, false, null, null, pListener);
    }

    public static void replace(ISource pFrom, IWritableSource pTo, ICopyListener pListener)
            throws IOException, IOSourceException {
        try {
            copy(pFrom, pTo, true, null, null, pListener);
        } catch (SourceAlreadyExistsException e) {
            Logger.e(e);
        }
    }

    public static void copy(ISource pFrom, IWritableSource pTo, boolean update,
            ISourceStreamParams pInParams,
            ISourceStreamParams pOutParams, ICopyListener pListener)
            throws IOException, IOSourceException, SourceAlreadyExistsException {
        if (pFrom.isConteiner()) {
            pTo.createConteiner();

            List<? extends ISource> children = pFrom.getChildren();
            
            for (ISource child : children) {
                IWritableSource toChild = pTo.getChild(child.getName());
                copy(child, toChild, update, null, null, null);
            }
            
        } else {

            if ((update) || (!pTo.exists())) {
                IWritableSource to = pTo;
                if (to.isConteiner()) {
                    to = to.getChild(pFrom.getName());
                }

                to.create();

                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    ISourceStreamParams inParam = getSaflyParams(pInParams);

                    inputStream = pFrom.openInputStream(inParam);
                    outputStream = to.openOutputStream(pOutParams);
                    long skipByte = inParam.getProperty(ParamNames.SKIP, 0L);
                    skipByte = inParam.getProperty(ParamNames.OUT_SKIP, skipByte);
                    long readed = inParam.getProperty(ParamNames.OUT_READED, 0L);
                    StreamUtils.copyStream(inputStream, outputStream, false, false, readed,
                            skipByte, pListener);

                } finally {
                    closeStream(pFrom, inputStream);
                    closeStream(to, outputStream);
                }
            } else {
                throw new SourceAlreadyExistsException();
            }

        }
    }

    public static void closeStream(ISource pSource, Closeable is) {
        if (is != null) {
            try {
                pSource.closeStream(is);
            } catch (IOException e) {
                Logger.e(e);
            }
        }
    }

    public static ISourceStreamParams getSaflyParams(ISourceStreamParams pParams) {
        if (pParams != null) {
            return pParams;
        } else {
            return new VoidParams();
        }
    }
}

