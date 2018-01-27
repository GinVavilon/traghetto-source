/**
 *
 */
package com.github.ginvavilon.traghentto.android;

import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.util.List;

import com.github.ginvavilon.traghentto.Source;

/**
 * @author Vladimir Baraznovsky
 *
 */
public interface AndroidSource extends Source {

    Uri getUri();

    AndroidSource getChild(String name);

    List<? extends AndroidSource> getChildren();
    ParcelFileDescriptor openParcelFileDescriptor() throws IOException;

}
