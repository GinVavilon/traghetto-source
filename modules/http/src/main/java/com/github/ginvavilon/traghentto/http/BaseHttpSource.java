/**
 * 
 */
package com.github.ginvavilon.traghentto.http;

import java.util.List;
import java.util.Map;

import com.github.ginvavilon.traghentto.BaseSource;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.params.ParamNames;
import com.github.ginvavilon.traghentto.params.SourceStreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public abstract class BaseHttpSource extends BaseSource implements Source, ParamNames {

    public static final String PARAM_HTTP_TIMEOUT = "http.timeout";
    protected static final String PARAM_HTTP_BUFFER = "http.buffer";
    protected static final String PARAM_DATA = "post.param";
    protected static final String CONTENT_LENGTH_HEADER = "Content-Length";
    protected long mLength = UNKNOWN_LENGTH;

    public static class HttpSourceParams extends SourceStreamParams {

        public HttpSourceParams timeout(int pValue) {
            return set(PARAM_HTTP_TIMEOUT, pValue);
        }

        public HttpSourceParams buffer(int pValue) {
            return set(PARAM_HTTP_BUFFER, pValue);
        }

        public HttpSourceParams data(Map<String, Object> pParams) {
            return set(PARAM_DATA, pParams);
        }

        @Override
        public HttpSourceParams set(String pKey, Object pValue) {
            return (HttpSourceParams) super.set(pKey, pValue);
        }

        public HttpSourceParams skip(long pLength) {
            return set(ParamNames.IN_SKIP, pLength);
        }
    }

    @Override
    public List<? extends Source> getChildren() {
        return null;
    }

    @Override
    public Source getChild(String pName) {
        return null;
    }

    @Override
    public boolean isContainer() {
        return false;
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
    public long getLength() {
        return mLength;
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