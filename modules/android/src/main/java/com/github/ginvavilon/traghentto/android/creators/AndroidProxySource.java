/**
 *
 */
package com.github.ginvavilon.traghentto.android.creators;

import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.ParseException;

import com.github.ginvavilon.traghentto.ISource;
import com.github.ginvavilon.traghentto.android.IAndroidSource;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.ISourceStreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
class AndroidProxySource implements IAndroidSource {

    private final ISource mSource;
    private final Uri mUri;

    public AndroidProxySource(ISource pSource, Uri uri) {
        mSource = pSource;
        mUri = uri;
    }

    public AndroidProxySource(ISource pSource) {
        this(pSource, Uri.parse(pSource.getUriString()));
    }

    @Override
    public boolean isConteiner() {
        return mSource.isConteiner();
    }

    @Override
    public InputStream openInputStream(ISourceStreamParams pParams)
            throws IOException, IOSourceException, ParseException {
        return mSource.openInputStream(pParams);
    }

    @Override
    public void closeStream(Closeable pStream) throws IOException {
        mSource.closeStream(pStream);
    }

    @Override
    public String getPath() {
        return mSource.getPath();
    }

    @Override
    public String getName() {
        return mSource.getName();
    }

    @Override
    public String getUriString() {
        return mSource.getUriString();
    }

    @Override
    public boolean exists() {
        return mSource.exists();
    }

    @Override
    public long getLenght() {
        return mSource.getLenght();
    }

    @Override
    public List<? extends AndroidProxySource> getChildren() {
        List<? extends ISource> children = mSource.getChildren();
        List<AndroidProxySource> list = new ArrayList<AndroidProxySource>();
        for (ISource child : children) {
            list.add(new AndroidProxySource(child));
        }
        return list;
    }

    public IAndroidSource getChild(String pName) {
        ISource child = mSource.getChild(pName);
        return child != null ? new AndroidProxySource(child) : null;
    }

    public boolean isLocal() {
        return mSource.isLocal();
    }

    public boolean isDataAvailable() {
        return mSource.isDataAvailable();
    }

    @Override
    public Uri getUri() {

        return mUri;
    }

    @Override
    public ParcelFileDescriptor openParcelFileDescriptor() throws IOException {
        return null;
    }

}
