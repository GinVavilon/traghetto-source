/**
 *
 */
package com.github.ginvavilon.traghentto.android;

import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.util.List;

import com.github.ginvavilon.traghentto.ISource;

/**
 * @author Vladimir Baraznovsky
 *
 */
public interface IAndroidSource extends ISource {

    Uri getUri();

    IAndroidSource getChild(String name);

    List<? extends IAndroidSource> getChildren();
    ParcelFileDescriptor openParcelFileDescriptor() throws IOException;

}
