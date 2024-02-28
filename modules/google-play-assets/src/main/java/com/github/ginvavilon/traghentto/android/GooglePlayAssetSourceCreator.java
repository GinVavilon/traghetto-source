package com.github.ginvavilon.traghentto.android;

import android.content.Context;
import android.net.Uri;

import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.android.creators.AndroidSourceCreator;

public class GooglePlayAssetSourceCreator implements AndroidSourceCreator<GooglePlayAssetSource> {

    public static void register() {
        SourceFactory.register(GooglePlayAssetSource.SCHEME, new GooglePlayAssetSourceCreator());
    }

    @Override
    public GooglePlayAssetSource create(Context context, Uri uri) {
        return new GooglePlayAssetSource(context,
                uri.getAuthority(),
                uri.getPath()
        );
    }
}
