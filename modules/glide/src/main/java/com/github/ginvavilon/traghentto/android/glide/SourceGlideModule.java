package com.github.ginvavilon.traghentto.android.glide;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.LibraryGlideModule;
import com.github.ginvavilon.traghentto.Source;

import java.io.InputStream;

@GlideModule
public class SourceGlideModule extends LibraryGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);
        registerModels(registry);
    }

    public static void registerModels(@NonNull Registry registry) {
        registry.prepend(Source.class, InputStream.class, new SourceModelLoader.Factory());
        registry.prepend(Source.class, Drawable.class, new DrawableSourceModelLoader.Factory());
    }
}
