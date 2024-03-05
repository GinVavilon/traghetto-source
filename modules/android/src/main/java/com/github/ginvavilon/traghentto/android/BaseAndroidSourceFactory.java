package com.github.ginvavilon.traghentto.android;

import android.net.Uri;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.android.creators.UriSourceCreator;
import com.github.ginvavilon.traghentto.file.CachedSource;
import com.github.ginvavilon.traghentto.file.DiskLruCache;

import java.util.Map;

public class BaseAndroidSourceFactory implements AndroidSourceFactory {

    private final Map<String, UriSourceCreator<?>> mCreators;
    private final UriSourceCreator<?> mDefault;
    private final AndroidSourceFactoryBuilder.ResourceCreator mResourceCreator;

    public BaseAndroidSourceFactory(UriSourceCreator<?> defaultCreator, AndroidSourceFactoryBuilder.ResourceCreator resourceCreator, Map<String, UriSourceCreator<?>> creators) {
        mCreators = creators;
        mDefault = defaultCreator;
        mResourceCreator = resourceCreator;
    }


    private Source createBasePathUri(Uri uri) {
        UriSourceCreator<?> creator = mCreators.get(uri.getScheme());
        if (creator == null) {
            creator = mDefault;
        }
        return creator.create(uri);
    }

    @Override
    public Source createFromUri(Uri uri) {
        Source source = createBasePathUri(uri);
        if (uri.getFragment() != null) {
            source = source.getChild(uri.getFragment());
        }
        return source;
    }

    @Override
    public Source createCachedIfNeed(Source pSource,
                                     DiskLruCache pDiskLruCache) {
        if (pSource.isLocal()) {
            return pSource;
        } else {
            return new CachedSource<Source>(pDiskLruCache, pSource);
        }
    }

    @Override
    public Source createFromResource(int pId) {
        return mResourceCreator.create(pId);
    }

    @Override
    public Source createFromUri(String pUri) {
        return createFromUri(Uri.parse(pUri));
    }

    @Override
    public Source createChild(Source pParent,
                              String pUri) {
        Uri uri = Uri.parse(pUri);
        Source source;
        if (uri.isRelative()) {
            source = pParent.getChild(uri.getPath());
        } else {
            source = createFromUri(pUri);
        }
        return source;
    }

    @Override
    public Source create(String uri) {
        return createFromUri(uri);
    }


}
