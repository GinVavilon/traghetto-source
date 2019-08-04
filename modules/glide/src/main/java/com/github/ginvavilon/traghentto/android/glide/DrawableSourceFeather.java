package com.github.ginvavilon.traghentto.android.glide;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.github.ginvavilon.traghentto.android.ResourceSource;

import static android.os.Build.VERSION.SDK_INT;

public class DrawableSourceFeather implements DataFetcher<Drawable> {
    private final ResourceSource mResourceSource;

    public DrawableSourceFeather(ResourceSource resourceSource) {
        mResourceSource = resourceSource;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Drawable> callback) {

        Drawable result = getDrawable();
        callback.onDataReady(result);
    }

    private Drawable getDrawable() {
        Resources res = mResourceSource.getResources();
        int id = mResourceSource.getId();
        Drawable result;
        if (SDK_INT >= 21) {
            result = res.getDrawable(id, null);
        } else {
            result = res.getDrawable(id);
        }
        return result;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void cancel() {

    }

    @NonNull
    @Override
    public Class<Drawable> getDataClass() {
        return Drawable.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }
}
