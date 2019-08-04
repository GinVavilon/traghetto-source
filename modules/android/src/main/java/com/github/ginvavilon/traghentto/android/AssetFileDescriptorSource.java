package com.github.ginvavilon.traghentto.android;

import android.content.res.AssetFileDescriptor;

import java.io.IOException;

/**
 * Created by vbaraznovsky on 03.12.18.
 */

public interface AssetFileDescriptorSource {

    AssetFileDescriptor openAssetFileDescriptor() throws IOException;

}
