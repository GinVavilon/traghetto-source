/**
 * 
 */
package com.github.ginvavilon.traghentto.http;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.github.ginvavilon.traghentto.ISource;
import com.github.ginvavilon.traghentto.params.ParamNames;
import com.github.ginvavilon.traghentto.params.SourceStreamParams;

/**
 * @author vbaraznovsky
 *
 */
public abstract class BaseHttpSource implements ISource, ParamNames {

    public static final String PARAM_HTTP_TIMEOUT = "http.timeout";
    protected static final String PARAM_HTTP_BUFFER = "http.buffer";
    protected static final String PARAM_DATA = "post.param";
    protected static final String CONTENT_LENGTH_HEADER = "Content-Length";
    protected long mLenght = UNKNOWN_LENGHT;

    public static class HttpSourceParams extends SourceStreamParams {

        public HttpSourceParams timeout(int pValue) {
            return set(PARAM_HTTP_TIMEOUT, pValue);
        }

        public HttpSourceParams bufer(int pValue) {
            return set(PARAM_HTTP_BUFFER, pValue);
        }

        public HttpSourceParams data(Map<String, Object> pParams) {
            return set(PARAM_DATA, pParams);
        }

        @Override
        public HttpSourceParams set(String pKey, Object pValue) {
            return (HttpSourceParams) super.set(pKey, pValue);
        }

        public HttpSourceParams skip(long pLenght) {
            return set(ParamNames.IN_SKIP, pLenght);
        }
    }

    @Override
    public List<? extends ISource> getChildren() {
        return null;
    }

    @Override
    public ISource getChild(String pName) {
        return null;
    }

    @Override
    public boolean isConteiner() {
        return false;
    }

    @Override
    public void closeStream(Closeable pStream) throws IOException {
        pStream.close();
    }

    @Override
    public String getName() {
        String name = getPath();
        int last = name.length() - 1;
        if ('/' == (name.charAt(last))) {
            last--;
        }
        int index = name.lastIndexOf("/", last);
        return name.substring(index + 1, last + 1);
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public long getLenght() {
        return mLenght;
    }

    @Override
    public String toString() {
        return getUriString().toString();
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isDataAvailable() {
        return false;
    }

    public BaseHttpSource() {
        super();
    }

}