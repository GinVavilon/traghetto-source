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
import com.github.ginvavilon.traghentto.exceptions.SourceAlreadyExistsException;
import com.github.ginvavilon.traghentto.params.ParamNames;
import com.github.ginvavilon.traghentto.params.StreamParams;
import com.github.ginvavilon.traghentto.params.VoidParams;

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
        if (pFrom.isConteiner()) {
            pTo.createConteiner();

            List<? extends Source> children = pFrom.getChildren();
            
            for (Source child : children) {
                WritableSource toChild = pTo.getChild(child.getName());
                copy(child, toChild, update, null, null, null);
            }
            
        } else {

            if ((update) || (!pTo.exists())) {
                WritableSource to = pTo;
                if (to.isConteiner()) {
                    to = to.getChild(pFrom.getName());
                }

                to.create();

                InputStream inputStream = null;
                StreamResource<InputStream> inputResource = null;
                StreamResource<OutputStream> outputResource = null;
                OutputStream outputStream = null;
                try {
                    StreamParams inParam = getSaflyParams(pInParams);
                    inputResource = pFrom.openResource(inParam);
                    outputResource = to.openOutputResource(pOutParams);
                    inputStream = inputResource.getStream();
                    outputStream = outputResource.getStream();
                    long skipByte = inParam.getProperty(ParamNames.SKIP, 0L);
                    skipByte = inParam.getProperty(ParamNames.OUT_SKIP, skipByte);
                    long readed = inParam.getProperty(ParamNames.OUT_READED, 0L);
                    StreamUtils.copyStream(inputStream, outputStream, false, false, readed,
                            skipByte, pListener);

                } finally {
                    StreamUtils.close(inputResource);
                    StreamUtils.close(outputResource);
                }
            } else {
                throw new SourceAlreadyExistsException();
            }

        }
    }



    public static StreamParams getSaflyParams(StreamParams pParams) {
        if (pParams != null) {
            return pParams;
        } else {
            return new VoidParams();
        }
    }
}

