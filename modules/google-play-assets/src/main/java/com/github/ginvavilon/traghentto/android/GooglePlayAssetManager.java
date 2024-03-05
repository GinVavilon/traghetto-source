package com.github.ginvavilon.traghentto.android;

import static com.google.android.play.core.assetpacks.model.AssetPackStatus.CANCELED;
import static com.google.android.play.core.assetpacks.model.AssetPackStatus.COMPLETED;
import static com.google.android.play.core.assetpacks.model.AssetPackStatus.DOWNLOADING;
import static com.google.android.play.core.assetpacks.model.AssetPackStatus.FAILED;
import static com.google.android.play.core.assetpacks.model.AssetPackStatus.NOT_INSTALLED;
import static com.google.android.play.core.assetpacks.model.AssetPackStatus.PENDING;
import static com.google.android.play.core.assetpacks.model.AssetPackStatus.TRANSFERRING;
import static com.google.android.play.core.assetpacks.model.AssetPackStatus.UNKNOWN;
import static com.google.android.play.core.assetpacks.model.AssetPackStatus.WAITING_FOR_WIFI;
import static com.google.android.play.core.assetpacks.model.AssetPackStorageMethod.APK_ASSETS;
import static com.google.android.play.core.assetpacks.model.AssetPackStorageMethod.STORAGE_FILES;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.PathUtils;
import com.github.ginvavilon.traghentto.RetrievableSource;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.file.FileSource;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.play.core.assetpacks.AssetPackLocation;
import com.google.android.play.core.assetpacks.AssetPackManager;
import com.google.android.play.core.assetpacks.AssetPackManagerFactory;
import com.google.android.play.core.assetpacks.AssetPackState;
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener;
import com.google.android.play.core.assetpacks.AssetPackStates;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class GooglePlayAssetManager {

    private final AssetManager mAssetManager;
    private final AssetPackManager mAssetPackManager;
    private final Map<RetrievableSource.Listener, Set<StateListener>> mListeners = new HashMap<>();

    public GooglePlayAssetManager(AssetManager assetManager, AssetPackManager assetPackManager) {
        mAssetManager = assetManager;
        mAssetPackManager = assetPackManager;
    }

    public GooglePlayAssetManager(Context context) {
        this(context.getAssets(), AssetPackManagerFactory.getInstance(context));
    }

    public Source getAssetSource(String packName, String path) {
        AssetPackLocation location = mAssetPackManager.getPackLocation(packName);
        if (location == null) {
            return null;
        }
        int storageMethod = location.packStorageMethod();
        String fileName;
        if (path.startsWith(PathUtils.PATH_SEPARATOR)) {
            fileName = path.substring(PathUtils.PATH_SEPARATOR.length());
        } else {
            fileName = path;
        }
        switch (storageMethod) {
            case APK_ASSETS:
                return new AssetSource(mAssetManager, fileName);
            case STORAGE_FILES:
                String assetsPath = location.assetsPath();
                if (assetsPath != null) {
                    return new FileSource(new File(assetsPath, fileName));
                } else {
                    return null;
                }
            default:
                return null;

        }
    }

    public RetrievableSource.Status getStatus(String packName) {
        AssetPackLocation packLocation = mAssetPackManager.getPackLocation(packName);
        if (packLocation != null) {
            return RetrievableSource.Status.READY;
        }

        AssetPackState packState;
        try {
            packState = getAssetPackState(packName);
        } catch (Exception e) {
            Logger.e(e);
            return RetrievableSource.Status.ERROR;
        }

        if (packState == null) {
            return RetrievableSource.Status.UNKNOWN;
        }
        return getAssetStatus(packState);
    }

    @NonNull
    private static RetrievableSource.Status getAssetStatus(AssetPackState packState) {
        switch (packState.status()) {
            case PENDING:
            case NOT_INSTALLED:
            case CANCELED:
                return RetrievableSource.Status.PENDING;
            case COMPLETED:
                return RetrievableSource.Status.READY;
            case DOWNLOADING:
            case TRANSFERRING:
                return RetrievableSource.Status.FETCHING;
            case FAILED:
                return RetrievableSource.Status.ERROR;
            case WAITING_FOR_WIFI:
                return RetrievableSource.Status.PAUSED;
            case UNKNOWN:
            default:
                return RetrievableSource.Status.UNKNOWN;
        }
    }

    @Nullable
    private AssetPackState getAssetPackState(String packName) throws Exception {
        Task<AssetPackStates> task = mAssetPackManager
                .getPackStates(Collections.singletonList(packName));
        Map<String, AssetPackState> packStates = Tasks
                .await(task)
                .packStates();

        AssetPackState packState = packStates.get(packName);
        return packState;
    }

    public Task<AssetPackStates> asyncRequest(String packName) {
        return mAssetPackManager.fetch(Collections.singletonList(packName));
    }

    private boolean isFinish(AssetPackState packState) {
        switch (packState.status()) {
            case PENDING:
            case DOWNLOADING:
            case NOT_INSTALLED:
            case TRANSFERRING:
            case WAITING_FOR_WIFI:
                return false;
            case UNKNOWN:
            case COMPLETED:
            case CANCELED:
            case FAILED:
            default:
                return true;
        }
    }

    public void waitReady(String packName) throws InterruptedException {

        ReentrantLock lock = new ReentrantLock();
        Condition readyCondition = lock.newCondition();
        AssetPackStateUpdateListener listener = assetPackState -> {
            try {
                lock.lock();
                if (packName.equals(assetPackState.name()) && isFinish(assetPackState)) {
                    readyCondition.signalAll();
                }
            } finally {
                lock.unlock();
            }
        };

        try {
            lock.lock();
            mAssetPackManager.registerListener(listener);
            asyncRequest(packName).addOnCompleteListener(task -> {
                        try {
                            lock.lock();
                            AssetPackState assetPackState = task.getResult().packStates().get(packName);
                            if (isFinish(assetPackState)) {
                                readyCondition.signalAll();
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
            );
            readyCondition.await();
        } finally {
            lock.unlock();
            mAssetPackManager.unregisterListener(listener);
        }

    }


    public boolean remove(String packName) {
        Task<Void> task = mAssetPackManager.removePack(packName);
        return Tasks.forResult(task).isSuccessful();
    }

    public boolean canBeRemoved(String packName) {
        AssetPackLocation location = mAssetPackManager.getPackLocation(packName);
        if (location != null) {
            return location.packStorageMethod() == STORAGE_FILES;
        } else {
            return false;
        }
    }

    public RetrievableSource.Controller createController(String packName, GooglePlayAssetSource source) {
        return new PlayAssetController(packName, source);
    }

    private class PlayAssetController implements RetrievableSource.Controller {

        private final List<String> mPackNames;
        private final String mPackName;
        private GooglePlayAssetSource mSource;

        public PlayAssetController(String packName, GooglePlayAssetSource source) {
            mPackName = packName;
            mSource = source;
            mPackNames = Collections.singletonList(mPackName);
        }

        @Override
        public void fetch() {
            mAssetPackManager.fetch(mPackNames);
        }

        @Override
        public void cancel() {
            mAssetPackManager.cancel(mPackNames);
        }

        @Override
        public RetrievableSource.Progress getProgress() {

            long bytesDownloaded;
            long totalBytesToDownload;
            AssetPackState packState;

            try {
                AssetPackStates assetPackStates = Tasks.await(mAssetPackManager.getPackStates(mPackNames));
                packState = assetPackStates.packStates().get(mPackName);
            } catch (ExecutionException | InterruptedException e) {
                packState = null;
            }

            if (packState != null) {
                bytesDownloaded = packState.bytesDownloaded();
                totalBytesToDownload = packState.totalBytesToDownload();
            } else {
                bytesDownloaded = Source.UNKNOWN_LENGTH;
                totalBytesToDownload = Source.UNKNOWN_LENGTH;
            }
            return new RetrievableSource.Progress() {
                @Override
                public long getFullByteSize() {
                    return bytesDownloaded;
                }

                @Override
                public long getReadyByteSize() {
                    return totalBytesToDownload;
                }
            };
        }


        @Override
        public void registerListener(RetrievableSource.Listener listener) {
            StateListener value = new StateListener(mSource, mPackName, listener);
            Set<StateListener> listeners;

            synchronized (mListeners) {
                listeners = mListeners.get(listener);
                if (listeners == null) {
                    listeners = new CopyOnWriteArraySet<>();
                }
            }
            listeners.add(value);
            mAssetPackManager.registerListener(value);
        }

        @Override
        public void unregisterListener(RetrievableSource.Listener listener) {
            Set<StateListener> listeners = mListeners.remove(listener);
            if (listeners!=null) {
                for (StateListener stateListener : listeners) {
                    mAssetPackManager.unregisterListener(stateListener);
                }
            }
        }
    }

    private static class StateListener implements AssetPackStateUpdateListener {
        private GooglePlayAssetSource mSource;
        private final String mPackName;
        private final RetrievableSource.Listener mListener;

        public StateListener(GooglePlayAssetSource source, String packName, RetrievableSource.Listener listener) {
            mSource = source;
            mPackName = packName;
            mListener = listener;
        }

        @Override
        public void onStateUpdate(@NonNull AssetPackState packState) {
            if (mPackName.equals(packState.name())) {
                RetrievableSource.Status status = getAssetStatus(packState);
                mListener.onStatusUpdate(mSource, status);
                switch (status) {
                    case FETCHING:
                        mListener.onProgress(mSource, packState.bytesDownloaded(), packState.totalBytesToDownload());
                        break;
                    case ERROR:
                        int errorCode = packState.errorCode();
                        mListener.onError(mSource, new IOSourceException("Fail download " + errorCode));
                        break;
                }
            }
        }
    }
}
