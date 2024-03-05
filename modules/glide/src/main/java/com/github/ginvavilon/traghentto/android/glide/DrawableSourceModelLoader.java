package com.github.ginvavilon.traghentto.android.glide;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.android.ResourceSource;

public class DrawableSourceModelLoader implements ModelLoader<Source, Drawable> {
    @Nullable
    @Override
    public LoadData<Drawable> buildLoadData(@NonNull Source source, int width, int height, @NonNull Options options) {

        ResourceSource resourceSource = (ResourceSource) source;
        Key key = new SourceKey(source);
        return new LoadData<>(key, new DrawableSourceFeather(resourceSource));
    }

    @Override
    public boolean handles(@NonNull Source source) {
        return source instanceof ResourceSource;
    }


    public static class Factory implements ModelLoaderFactory<Source, Drawable> {

        @NonNull
        @Override
        public ModelLoader<Source, Drawable> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new DrawableSourceModelLoader();
        }

        @Override
        public void teardown() {

        }
    }
}
