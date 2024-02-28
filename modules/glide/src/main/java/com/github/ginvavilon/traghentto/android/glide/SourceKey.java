package com.github.ginvavilon.traghentto.android.glide;


import androidx.annotation.NonNull;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.util.Preconditions;
import com.github.ginvavilon.traghentto.Source;

import java.security.MessageDigest;


public class SourceKey implements Key {
    @NonNull
    private final Source mSource;

    public SourceKey(Source source) {
        mSource = Preconditions.checkNotNull(source);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SourceKey sourceKey = (SourceKey) o;

        return mSource.equals(sourceKey.mSource);
    }

    @Override
    public int hashCode() {
        return mSource.hashCode();
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(mSource.getUriString().getBytes());
    }
}
