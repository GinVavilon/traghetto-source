/**
 * 
 */
package com.github.ginvavilon.traghentto.android;

import android.os.ParcelFileDescriptor;

import java.io.IOException;

import com.github.ginvavilon.traghentto.Source;

/**
 * @author vbaraznovsky
 *
 */
public interface ParselFileDesriptorSource extends Source {

    ParcelFileDescriptor openParcelFileDescriptor() throws IOException;

}