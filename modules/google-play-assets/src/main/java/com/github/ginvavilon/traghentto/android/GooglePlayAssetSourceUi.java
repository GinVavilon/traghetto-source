package com.github.ginvavilon.traghentto.android;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.app.Activity;

import androidx.core.app.ComponentActivity;
import androidx.core.util.Consumer;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.play.core.assetpacks.AssetPackManager;
import com.google.android.play.core.assetpacks.AssetPackManagerFactory;
import com.google.android.play.core.assetpacks.AssetPackState;
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener;
import com.google.android.play.core.assetpacks.model.AssetPackStatus;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class GooglePlayAssetSourceUi {

    private final Activity activity;
    private AssetPackManager assetPackManager;
    private final Consumer<Boolean> shownCallback;

    private final Set<String> requestedShown = new CopyOnWriteArraySet<>();

    private final AssetPackStateUpdateListener assetPackStateUpdateListener = new AssetPackStateUpdateListener() {

        @Override
        public void onStateUpdate(AssetPackState assetPackState) {
            switch (assetPackState.status()) {
                case AssetPackStatus.PENDING:
                case AssetPackStatus.DOWNLOADING:
                case AssetPackStatus.TRANSFERRING:
                case AssetPackStatus.COMPLETED:
                case AssetPackStatus.FAILED:
                case AssetPackStatus.CANCELED:
                case AssetPackStatus.NOT_INSTALLED:
                case AssetPackStatus.UNKNOWN:
                    break;
                case AssetPackStatus.WAITING_FOR_WIFI:
                    if (requestedShown.add(assetPackState.name())) {
                        assetPackManager.showCellularDataConfirmation(activity)
                                .addOnSuccessListener(new OnSuccessListener<Integer>() {
                                    @Override
                                    public void onSuccess(Integer resultCode) {
                                        shownCallback.accept(resultCode == RESULT_OK);
                                    }
                                });
                    }
                    break;
            }
        }
    };

    public GooglePlayAssetSourceUi(Activity activity, Consumer<Boolean> shownCallback) {
        this.activity = activity;
        this.shownCallback = shownCallback;
    }

    public GooglePlayAssetSourceUi(Activity activity) {
        this(activity, (it) -> {
        });
    }

    public void start() {
        this.assetPackManager = AssetPackManagerFactory.getInstance(activity);
        assetPackManager.registerListener(assetPackStateUpdateListener);
    }

    public void stop() {
        assetPackManager.unregisterListener(assetPackStateUpdateListener);
    }

    public void clear() {
        requestedShown.clear();
    }

}
