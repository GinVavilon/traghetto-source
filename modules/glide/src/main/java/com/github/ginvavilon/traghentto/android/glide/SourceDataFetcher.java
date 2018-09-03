package com.github.ginvavilon.traghentto.android.glide;

import android.support.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

import java.io.IOException;
import java.io.InputStream;

public class SourceDataFetcher implements DataFetcher<InputStream> {

    private final Source mSource;
    private StreamParams mParams;
    private StreamResource<InputStream> mResource;

    public SourceDataFetcher(Source source, StreamParams params) {
        mSource = source;
        mParams = params;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {

        try {
            mResource = mSource.openResource(mParams);
            callback.onDataReady(mResource.getStream());
        } catch (IOException | IOSourceException e) {
            e.printStackTrace();
            callback.onLoadFailed(e);
        }

    }

    @Override
    public void cleanup() {
        try {
            if (mResource != null) {
                mResource.close();
            }
        } catch (IOException e) {
            Logger.e(e);
        }

    }

    @Override
    public void cancel() {
    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        if (mSource.isLocal()) {
            return DataSource.LOCAL;
        } else {
            return DataSource.REMOTE;
        }
    }
}
