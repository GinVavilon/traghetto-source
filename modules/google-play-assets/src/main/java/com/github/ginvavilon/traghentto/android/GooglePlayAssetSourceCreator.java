package com.github.ginvavilon.traghentto.android;

import android.content.Context;
import android.net.Uri;

import com.github.ginvavilon.traghentto.android.creators.UriSourceCreator;
import com.github.ginvavilon.traghentto.android.creators.WithDefaultScheme;

public class GooglePlayAssetSourceCreator implements UriSourceCreator<GooglePlayAssetSource>, WithDefaultScheme {

    private GooglePlayAssetManager mGooglePlayAssetManager;


    public GooglePlayAssetSourceCreator(Context context) {
        mGooglePlayAssetManager = new GooglePlayAssetManager(context);
    }

    @Override
    public GooglePlayAssetSource create(Uri uri) {
        return new GooglePlayAssetSource(mGooglePlayAssetManager,
                uri.getAuthority(),
                uri.getPath()
        );
    }

    @Override
    public String getDefaultScheme() {
        return GooglePlayAssetSource.SCHEME;
    }
}
