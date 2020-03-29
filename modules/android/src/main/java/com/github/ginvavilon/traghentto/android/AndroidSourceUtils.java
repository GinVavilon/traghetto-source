/**
 * 
 */
package com.github.ginvavilon.traghentto.android;

import android.os.ParcelFileDescriptor;

import java.io.IOException;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.file.FileSource;

/**
 * @author Vladimir Baraznovsky
 *
 */
public final class AndroidSourceUtils {

    private AndroidSourceUtils() {
    }

    public static ParcelFileDescriptor openParcelFileDescriptor(Source source) throws IOException {
        if (source instanceof ParcelFileDescriptorSource) {
            ParcelFileDescriptorSource parcelFileDescriptorSource = (ParcelFileDescriptorSource) source;
            return parcelFileDescriptorSource.openParcelFileDescriptor();
        }
        if (source instanceof FileSource) {
            FileSource fileSource = (FileSource) source;
            return ParcelFileDescriptor.open(fileSource.getFile(),
                    ParcelFileDescriptor.MODE_READ_ONLY);

        }
        throw new IOException("Source is not support file description");
    }

}
