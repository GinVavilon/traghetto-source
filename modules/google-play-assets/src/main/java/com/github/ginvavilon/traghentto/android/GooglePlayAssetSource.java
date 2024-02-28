package com.github.ginvavilon.traghentto.android;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.github.ginvavilon.traghentto.DelegatedSource;
import com.github.ginvavilon.traghentto.DeletableSource;
import com.github.ginvavilon.traghentto.PathUtils;
import com.github.ginvavilon.traghentto.RetrievableSource;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceIterator;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class GooglePlayAssetSource extends DelegatedSource<Source> implements RetrievableSource, DeletableSource {

    public static final String SCHEME = "gp-asset";

    private final GooglePlayAssetManager mAssetPackManager;
    private final String mPackName;
    private final String mPath;
    private final String mUri;
    private final Controller mController;

    public GooglePlayAssetSource(Context context, String packName, String path) {
        this(new GooglePlayAssetManager(context.getApplicationContext()), packName, path);
    }

    GooglePlayAssetSource(GooglePlayAssetManager assetPackManager, String packName, String path) {
        super(createProvider(assetPackManager, packName, path));
        mAssetPackManager = assetPackManager;
        mPackName = packName;
        mPath = path;
        mUri = createUri(packName, path);
        mController = mAssetPackManager.createController(mPackName, this);
    }

    @Override
    public Source getChild(String name) {
        String childPath = PathUtils.concat(mPath, name);
        return new GooglePlayAssetSource(mAssetPackManager, mPackName, childPath);
    }


    @Override
    public StreamResource<InputStream> openResource(StreamParams pParams) throws IOSourceException, IOException {
        Status status = getStatus();
        if (status == Status.PENDING) {
            try {
                mAssetPackManager.waitReady(mPackName);
            } catch (InterruptedException e) {
                throw new IOSourceException(e);
            }
        }
        return super.openResource(pParams);
    }

    @Override
    public String getUriString() {
        return mUri;
    }

    @Override
    public String getPath() {
        return mPackName;
    }

    @Override
    public String getName() {
        return PathUtils.extractName(mPath);
    }

    @NonNull
    @Override
    public String toString() {
        return getUriString();
    }

    private static SourceProvider<Source> createProvider(GooglePlayAssetManager assetPackManager, String packName, String path) {
        return new SourceProvider<Source>() {

            private final Source unavailable = new UnavailableSource(packName, path);
            private Source base;

            @Override
            public Source getSource() {
                if (base != null) {
                    return base;
                }
                synchronized (this) {
                    if (base != null) {
                        return base;
                    }
                    base = assetPackManager.getAssetSource(packName, path);
                    if (base != null) {
                        return base;
                    } else {
                        return unavailable;
                    }

                }
            }

            @Override
            public void clear() {
                synchronized (this) {
                    base = null;
                }
            }
        };
    }

    @Override
    public Status getStatus() {
        return mAssetPackManager.getStatus(mPackName);
    }

    @Override
    public Controller getController() {
        return mController;
    }

    @Override
    public boolean delete() {
        boolean removed = mAssetPackManager.remove(mPackName);
        if (removed) {
            mSourceProvider.clear();
        }
        return removed;
    }

    @Override
    public boolean canBeDeleted() {
        return mAssetPackManager.canBeRemoved(mPackName);
    }


    private static class UnavailableSource implements Source {

        private final String mPackName;
        private final String mPath;

        public UnavailableSource(String packName, String path) {
            mPackName = packName;
            mPath = path;
        }

        @Override
        public List<? extends Source> getChildren() {
            throw new IllegalStateException(getUriString() + " is not available");
        }

        @Override
        public Source getChild(String name) {
            return null;
        }

        @Override
        public boolean isContainer() {
            return false;
        }

        @Override
        public StreamResource<InputStream> openResource(StreamParams pParams) throws IOSourceException, IOException {
            throw new IllegalStateException(getUriString() + " is not available");
        }

        @Override
        public String getPath() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getUriString() {
            return createUri(mPackName, mPath);
        }

        @Override
        public boolean exists() {
            return false;
        }

        @Override
        public long getLength() {
            return UNKNOWN_LENGTH;
        }

        @Override
        public boolean isLocal() {
            return false;
        }

        @Override
        public boolean isDataAvailable() {
            return false;
        }

        @Override
        public SourceIterator iterator() {
            throw new IllegalStateException(getUriString() + " is not available");
        }

    }

    private static String createUri(String packName, String path) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME);
        builder.authority(packName);
        builder.path(path);
        return builder.build().toString();
    }

}
