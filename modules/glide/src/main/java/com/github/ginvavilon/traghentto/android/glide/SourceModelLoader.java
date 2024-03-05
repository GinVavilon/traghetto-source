package com.github.ginvavilon.traghentto.android.glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.params.VoidParams;

import java.io.InputStream;


public class SourceModelLoader implements ModelLoader<Source, InputStream> {

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull Source source, int width, int height, @NonNull Options options) {

        Key key = new SourceKey(source);
        return new LoadData<>(key, new SourceDataFetcher(source, new VoidParams()));
    }

    @Override
    public boolean handles(@NonNull Source source) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<Source, InputStream> {

        @NonNull
        @Override
        public ModelLoader<Source, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new SourceModelLoader();
        }

        @Override
        public void teardown() {

        }
    }
}
