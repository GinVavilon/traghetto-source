/**
 *
 */
package com.github.ginvavilon.traghentto.android;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

import com.github.ginvavilon.traghentto.SourceCreator;
import com.github.ginvavilon.traghentto.UriConstants;
import com.github.ginvavilon.traghentto.android.creators.AndroidSourceCreator;
import com.github.ginvavilon.traghentto.android.creators.PathAndroidCreator;
import com.github.ginvavilon.traghentto.android.creators.UriAndroidCreator;
import com.github.ginvavilon.traghentto.file.DiskLruCache;
import com.github.ginvavilon.traghentto.http.apache.ApacheHttpSource;
import com.github.ginvavilon.traghentto.zip.ZipRandomAccessFileSource;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class SourceFactory implements UriConstants {

    private static final Map<String, AndroidSourceCreator<?>> sCreators = new HashMap<>();
    private static AndroidSourceCreator<?> sDefault;

    static {

        register(HTTPS_SCHEME, ApacheHttpSource.CREATOR);
        register(HTTP_SCHEME, ApacheHttpSource.CREATOR);
        registerPath(ZIP_FILE_SCHEME, ZipRandomAccessFileSource.CREATOR);
        register(FILE_SCHEME, AndroidFileSource.ANDROID_CREATOR);
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

    private static IAndroidSource createBasePathUri(Context pContext, Uri uri) {
        AndroidSourceCreator<?> creator = sCreators.get(uri.getScheme());
        if (creator == null) {
            creator = sDefault;
        }

        return creator.create(pContext, uri);

    }

    public static IAndroidSource createFromUri(Context pContext, Uri uri) {
        IAndroidSource source = createBasePathUri(pContext, uri);
        if (uri.getFragment() != null) {
            source = source.getChild(uri.getFragment());
        }
        return source;
    }

    public static IAndroidSource createCachedIfNeed(IAndroidSource pSource,
            DiskLruCache pDiskLruCache) {
        if (pSource.isLocal()) {
            return pSource;
        } else {
            return new AndroidCachedSource<IAndroidSource>(pDiskLruCache, pSource);
        }
    }

    public static ResourceSource createFromResource(Context pContext, int pId) {
        Resources resources = pContext.getResources();
        return new ResourceSource(resources, pId);
    }

    public static IAndroidSource createFromUri(Context pContext, String pUri) {
        return createFromUri(pContext, Uri.parse(pUri));
    }

    public static IAndroidSource createChild(Context pContext, IAndroidSource pParent,
            String pUri) {
        Uri uri = Uri.parse(pUri);
        IAndroidSource source;
        if (uri.isRelative()) {
            source = pParent.getChild(uri.getPath());
        } else {
            source = createFromUri(pContext, pUri);
        }
        return source;
    };

}
