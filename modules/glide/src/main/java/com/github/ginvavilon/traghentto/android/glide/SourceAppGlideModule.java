package com.github.ginvavilon.traghentto.android.glide;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class SourceAppGlideModule extends AppGlideModule {

    private SourceGlideModule mSourceGlideModule = new SourceGlideModule();
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);
        mSourceGlideModule.registerComponents(context, glide, registry);
    }

}
