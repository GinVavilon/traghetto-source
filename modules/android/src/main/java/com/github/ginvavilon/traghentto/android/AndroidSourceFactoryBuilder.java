package com.github.ginvavilon.traghentto.android;

import android.content.Context;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceCreator;
import com.github.ginvavilon.traghentto.android.creators.AndroidSourceCreator;
import com.github.ginvavilon.traghentto.android.creators.UriSourceCreator;
import com.github.ginvavilon.traghentto.android.creators.WithDefaultScheme;

import java.util.HashMap;
import java.util.Map;

public final class AndroidSourceFactoryBuilder {

    private final Map<String, UriSourceCreator<?>> mCreators = new HashMap<>();
    private UriSourceCreator<?> mDefault;
    private ResourceCreator mResourceCreator;

    public AndroidSourceFactoryBuilder register(String scheme, UriSourceCreator<?> creator) {
        mCreators.put(scheme, creator);
        return this;
    }

    public AndroidSourceFactoryBuilder register(String scheme, Context context, AndroidSourceCreator<?> creator) {
        register(scheme, wrap(context, creator));
        return this;
    }

    public AndroidSourceFactoryBuilder registerPath(String scheme, SourceCreator<?> creator) {
        register(scheme, wrapAsPath(creator));
        return this;
    }

    public AndroidSourceFactoryBuilder register(String scheme, SourceCreator<?> creator) {
        register(scheme, wrap(creator));
        return this;
    }

    public <Creator extends UriSourceCreator & WithDefaultScheme> AndroidSourceFactoryBuilder register(Creator creator) {
        register(creator.getDefaultScheme(), creator);
        return this;
    }

    public AndroidSourceFactoryBuilder setDefault(Context context, AndroidSourceCreator<?> creator) {
        mDefault = wrap(context, creator);
        return this;
    }

    public AndroidSourceFactoryBuilder setDefault(UriSourceCreator<?> creator) {
        mDefault = creator;
        return this;
    }

    public AndroidSourceFactoryBuilder setDefault(SourceCreator<?> creator) {
        mDefault = wrap(creator);
        return this;
    }

    public AndroidSourceFactoryBuilder setResourceCreator(ResourceCreator resourceCreator) {
        mResourceCreator = resourceCreator;
        return this;
    }

    public AndroidSourceFactory build() {
        return new BaseAndroidSourceFactory(mDefault, mResourceCreator, new HashMap<>(mCreators));
    }

    private static <T extends Source> UriSourceCreator<T> wrap(Context context, AndroidSourceCreator<T> creator) {
        return uri -> creator.create(context, uri);
    }

    private static <T extends Source> UriSourceCreator<T> wrapAsPath(SourceCreator<T> creator) {
        return uri -> creator.create(uri.getPath());
    }

    private static <T extends Source> UriSourceCreator<T> wrap(SourceCreator<T> creator) {
        return uri -> creator.create(uri.toString());
    }


    public interface ResourceCreator {
        Source create(int resId);
    }
}
