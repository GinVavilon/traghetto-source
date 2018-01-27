/**
 *
 */
package com.github.ginvavilon.traghentto.android;

import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.github.ginvavilon.traghentto.http.apache.ApacheHttpSource;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class AndroidHttpSource extends ApacheHttpSource implements AndroidSource {

    private Uri mUri;

    public AndroidHttpSource(Uri pUri) {
        super(URI.create(pUri.toString()));
        mUri = pUri;
    }

    @Override
    public AndroidHttpSource getChild(String pName) {

        return null;
    }

    @Override
    public List<AndroidHttpSource> getChildren() {
        return null;
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
