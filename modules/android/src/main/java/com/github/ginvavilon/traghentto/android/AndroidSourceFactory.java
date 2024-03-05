package com.github.ginvavilon.traghentto.android;

import static com.github.ginvavilon.traghentto.UriConstants.ASSET_SCHEME;
import static com.github.ginvavilon.traghentto.UriConstants.FILE_SCHEME;
import static com.github.ginvavilon.traghentto.UriConstants.HTTPS_SCHEME;
import static com.github.ginvavilon.traghentto.UriConstants.HTTP_SCHEME;
import static com.github.ginvavilon.traghentto.UriConstants.RESOURCE_SCHEME;
import static com.github.ginvavilon.traghentto.UriConstants.ZIP_FILE_SCHEME;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceFactory;
import com.github.ginvavilon.traghentto.file.DiskLruCache;
import com.github.ginvavilon.traghentto.file.FileSource;
import com.github.ginvavilon.traghentto.http.apache.ApacheHttpSource;
import com.github.ginvavilon.traghentto.zip.ZipRandomAccessFileSource;

public interface AndroidSourceFactory extends SourceFactory {

    Source createFromUri(Uri uri);

    Source createCachedIfNeed(Source pSource,
                              DiskLruCache pDiskLruCache);

    Source createFromResource(int pId);

    Source createFromUri(String pUri);

    Source createChild(Source pParent,
                       String pUri);

    @Override
    Source create(String uri);

    static AndroidSourceFactory createDefault(Context context) {
        AndroidSourceFactoryBuilder builder = createDefaultBuilder(context);
        return builder.build();
    }


    static AndroidSourceFactory getInstance(Context context) {
        if (context instanceof AndroidSourceFactoryOwner) {
            return ((AndroidSourceFactoryOwner) context).getAndroidSourceFactory();
        } else {
            Context applicationContext = context.getApplicationContext();
            if (applicationContext instanceof AndroidSourceFactoryOwner) {
                return ((AndroidSourceFactoryOwner) applicationContext).getAndroidSourceFactory();
            } else {
                return createDefault(context);
            }
        }
    }

    @NonNull
    static AndroidSourceFactoryBuilder createDefaultBuilder(Context context) {
        AndroidSourceFactoryBuilder builder = new AndroidSourceFactoryBuilder();
        try {
            Class.forName("com.github.ginvavilon.traghentto.http.apache.ApacheHttpSource");
            builder.register(HTTPS_SCHEME, ApacheHttpSource.CREATOR);
            builder.register(HTTP_SCHEME, ApacheHttpSource.CREATOR);
        } catch (ClassNotFoundException e) {
        }
        builder.registerPath(ZIP_FILE_SCHEME, ZipRandomAccessFileSource.CREATOR)
                .registerPath(FILE_SCHEME, FileSource.CREATOR)
                .register(ContentResolver.SCHEME_ANDROID_RESOURCE, context, ResourceSource.ANDROID_CREATOR)
                .register(ContentResolver.SCHEME_CONTENT, context, DocumentSource.ANDROID_CREATOR)
                .register(RESOURCE_SCHEME, context, ResourceSource.ANDROID_CREATOR)
                .register(ASSET_SCHEME, context, AssetSource.ANDROID_CREATOR)
                .setDefault(context, AssetSource.ANDROID_CREATOR)
                .setResourceCreator(id -> new ResourceSource(context.getResources(), id));
        return builder;
    }
}
