/**
 *
 */
package com.github.ginvavilon.traghentto.http.apache;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.Logger.Level;
import com.github.ginvavilon.traghentto.SourceCreator;
import com.github.ginvavilon.traghentto.SourceUtils;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.http.BaseHttpSource;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class ApacheHttpSource extends BaseHttpSource {


    private static final Pattern RANGE_HEADER_PATTER = Pattern
            .compile("bytes ([0-9]+)-[0-9]*/([0-9]+)");
    final private URI mUrl;

    public ApacheHttpSource(URI pUrl) {
        super();
        mUrl = pUrl;
    }

    @Override
    public InputStream openInputStream(StreamParams pParams)
            throws IOException, IOSourceException {
        Logger.d(Level.HTTP | Level.SOURCE | Level.STREAM, "Open Stream");
        StreamParams params = SourceUtils.getSafetyParams(pParams);
        Object data = params.getProperty(PARAM_DATA, (Object) null);
        if (data != null) {
            return openInputStream(params, (Map<String, Object>) data);
        }
        HttpGet request = new HttpGet(mUrl);
        return openInputStream(params, request);
    }

    private InputStream openInputStream(StreamParams pParams, HttpUriRequest pRequest)
            throws IOException, ClientProtocolException, IOSourceException {
        Logger.d(Level.HTTP | Level.SOURCE, "Create Http Client");
        HttpClient client = new DefaultHttpClient();
        HttpParams params = client.getParams();
        HttpConnectionParams.setSocketBufferSize(params,
                pParams.getProperty(PARAM_HTTP_BUFFER, 1024));
        int timeout = pParams.getProperty(PARAM_HTTP_TIMEOUT, 50000);
        HttpConnectionParams.setConnectionTimeout(params, timeout);
        HttpConnectionParams.setSoTimeout(params, timeout);
        long skip = pParams.getProperty(IN_SKIP, 0L);
        if (skip > 0) {
            pRequest.setHeader("Range", "bytes=" + skip + "-");
        }
        Logger.d(Level.HTTP | Level.SOURCE, "Create get Header");
        Header[] allHeaders = pRequest.getAllHeaders();
        for (Header header : allHeaders) {
            Logger.d(Level.HTTP | Level.SOURCE, "Request Header %s:%s", header.getName(),
                    header.getValue());
        }
        Logger.d(Level.HTTP | Level.SOURCE, "Execute");
        HttpResponse response = client.execute(pRequest);

        Logger.d(Level.HTTP | Level.SOURCE, "Create get Header");
        allHeaders = response.getAllHeaders();
        for (Header header : allHeaders) {
            Logger.d(Level.HTTP | Level.SOURCE, "Response Header %s:%s", header.getName(),
                    header.getValue());
        }

        Logger.d(Level.HTTP | Level.SOURCE, "Create get Entity");
        HttpEntity entity = response.getEntity();
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            IOSourceException exception = new IOSourceException(
                    response.getStatusLine().toString());
            exception.setData(EntityUtils.toString(entity));
            throw exception;
        }
        mLength = entity.getContentLength();
        Header rangeHeader = response.getFirstHeader("Content-Range");
        if (rangeHeader != null) {
            Matcher rangeMatcher = RANGE_HEADER_PATTER.matcher(rangeHeader.getValue());
            long skipped = 0;
            if (rangeMatcher.matches()) {
                String group = rangeMatcher.group(1);
                String group2 = rangeMatcher.group(2);

                skipped = Long.parseLong(group);
                mLength = Long.parseLong(group2);
            }
            pParams.changeProperty(OUT_SKIP, skip - skipped);
            pParams.changeProperty(OUT_ALREADY_READ, skipped);
        }

        Logger.d(Level.HTTP | Level.SOURCE, "Create get Content");
        return entity.getContent();
    }

    public InputStream openInputStream(StreamParams pHttpParams, Map<String, Object> pParams)
            throws IOException, IOSourceException {
        HttpPost request = new HttpPost(mUrl);
        List<NameValuePair> parameters = new LinkedList<NameValuePair>();
        Set<String> keys = pParams.keySet();
        for (String key : keys) {
            Object value = pParams.get(key);
            parameters.add(new BasicNameValuePair(key, String.valueOf(value)));
        }

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters);
        request.setEntity(entity);
        return openInputStream(pHttpParams, request);
    }

    @Override
    public String getPath() {
        return mUrl.getPath();
    }

    @Override
    public String getUriString() {
        return mUrl.toString();
    }

    public static final SourceCreator<ApacheHttpSource> CREATOR = new SourceCreator<ApacheHttpSource>() {

        @Override
        public ApacheHttpSource create(String pParam) {
            return new ApacheHttpSource(URI.create(pParam));
        }
    };

}
