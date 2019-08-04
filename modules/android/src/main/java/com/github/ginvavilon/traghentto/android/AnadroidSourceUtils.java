/**
 * 
 */
package com.github.ginvavilon.traghentto.android;

import android.os.ParcelFileDescriptor;

import java.io.IOException;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.file.FileSource;

/**
 * @author vbaraznovsky
 *
 */
public final class AnadroidSourceUtils {

    private AnadroidSourceUtils() {
    }

    public static ParcelFileDescriptor openParselFileDescriptor(Source source) throws IOException {
        if (source instanceof ParselFileDesriptorSource) {
            ParselFileDesriptorSource parselFileDesriptorSource = (ParselFileDesriptorSource) source;
            return parselFileDesriptorSource.openParcelFileDescriptor();
        }
        if (source instanceof FileSource) {
            FileSource fileSource = (FileSource) source;
            return ParcelFileDescriptor.open(fileSource.getFile(),
                    ParcelFileDescriptor.MODE_READ_ONLY);

        }
        throw new IOException("Source is not soopport file description");
    }

}
