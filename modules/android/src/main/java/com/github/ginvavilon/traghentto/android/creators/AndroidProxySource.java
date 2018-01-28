/**
 *
 */
package com.github.ginvavilon.traghentto.android.creators;

import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.ginvavilon.traghentto.DelegatedSource;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.android.AndroidSource;

/**
 * @author Vladimir Baraznovsky
 *
 */
class AndroidProxySource extends DelegatedSource<Source> implements AndroidSource {

    private final Uri mUri;

    public AndroidProxySource(Source pSource, Uri uri) {
        super(pSource);
        mUri = uri;
    }

    public AndroidProxySource(Source pSource) {
        this(pSource, Uri.parse(pSource.getUriString()));
    }

    @Override
    public List<? extends AndroidProxySource> getChildren() {
        List<? extends Source> children = mSource.getChildren();
        List<AndroidProxySource> list = new ArrayList<AndroidProxySource>();
        for (Source child : children) {
            list.add(new AndroidProxySource(child));
        }
        return list;
    }

    public AndroidSource getChild(String pName) {
        Source child = mSource.getChild(pName);
        return child != null ? new AndroidProxySource(child) : null;
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
