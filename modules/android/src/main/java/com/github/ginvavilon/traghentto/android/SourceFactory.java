/**
 *
 */
package com.github.ginvavilon.traghentto.android;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceCreator;
import com.github.ginvavilon.traghentto.UriConstants;
import com.github.ginvavilon.traghentto.android.creators.AndroidSourceCreator;
import com.github.ginvavilon.traghentto.android.creators.PathAndroidCreator;
import com.github.ginvavilon.traghentto.android.creators.UriAndroidCreator;
import com.github.ginvavilon.traghentto.file.CachedSource;
import com.github.ginvavilon.traghentto.file.DiskLruCache;
import com.github.ginvavilon.traghentto.file.FileSource;
import com.github.ginvavilon.traghentto.http.apache.ApacheHttpSource;
import com.github.ginvavilon.traghentto.zip.ZipRandomAccessFileSource;

/**
 * @author Vladimir Baraznovsky
 *
 */
@Deprecated
public class SourceFactory implements UriConstants {

    private static final Map<String, AndroidSourceCreator<?>> sCreators = new HashMap<>();
    private static AndroidSourceCreator<?> sDefault;

    static {

        try {
            Class.forName("com.github.ginvavilon.traghentto.http.apache.ApacheHttpSource");
            register(HTTPS_SCHEME, ApacheHttpSource.CREATOR);
            register(HTTP_SCHEME, ApacheHttpSource.CREATOR);
        } catch (ClassNotFoundException e) {
        }

        registerPath(ZIP_FILE_SCHEME, ZipRandomAccessFileSource.CREATOR);
        registerPath(FILE_SCHEME, FileSource.CREATOR);
        register(ContentResolver.SCHEME_ANDROID_RESOURCE, ResourceSource.ANDROID_CREATOR);
        register(ContentResolver.SCHEME_CONTENT, DocumentSource.ANDROID_CREATOR);
        register(RESOURCE_SCHEME, ResourceSource.ANDROID_CREATOR);
        register(ASSET_SCHEME, AssetSource.ANDROID_CREATOR);
        setDefault(AssetSource.ANDROID_CREATOR);

    }

    public static void register(String scheme, AndroidSourceCreator<?> creator) {
        sCreators.put(scheme, creator);
    }

    public static void registerPath(String scheme, SourceCreator<?> creator) {
        register(scheme, new PathAndroidCreator(creator));
    }

    public static void register(String scheme, SourceCreator<?> creator) {
        register(scheme, new UriAndroidCreator(creator));
    }

    public static void setDefault(AndroidSourceCreator<?> creator) {
        sDefault = creator;
    }

    private static Source createBasePathUri(Context pContext, Uri uri) {
        AndroidSourceCreator<?> creator = sCreators.get(uri.getScheme());
        if (creator == null) {
            creator = sDefault;
        }

        return creator.create(pContext, uri);

    }

    public static Source createFromUri(Context pContext, Uri uri) {
        Source source = createBasePathUri(pContext, uri);
        if (uri.getFragment() != null) {
            source = source.getChild(uri.getFragment());
        }
        return source;
    }

    public static Source createCachedIfNeed(Source pSource,
            DiskLruCache pDiskLruCache) {
        if (pSource.isLocal()) {
            return pSource;
        } else {
            return new CachedSource<Source>(pDiskLruCache, pSource);
        }
    }

    public static ResourceSource createFromResource(Context pContext, int pId) {
        Resources resources = pContext.getResources();
        return new ResourceSource(resources, pId);
    }

    public static Source createFromUri(Context pContext, String pUri) {
        return createFromUri(pContext, Uri.parse(pUri));
    }

    public static Source createChild(Context pContext, Source pParent,
            String pUri) {
        Uri uri = Uri.parse(pUri);
        Source source;
        if (uri.isRelative()) {
            source = pParent.getChild(uri.getPath());
        } else {
            source = createFromUri(pContext, pUri);
        }
        return source;
    };

}
