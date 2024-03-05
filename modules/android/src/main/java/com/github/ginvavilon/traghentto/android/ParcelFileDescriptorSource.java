/**
 * 
 */
package com.github.ginvavilon.traghentto.android;

import android.os.ParcelFileDescriptor;

import java.io.IOException;

import com.github.ginvavilon.traghentto.Source;

/**
 * @author Vladimir Baraznovsky
 *
 */
public interface ParcelFileDescriptorSource extends Source {

    ParcelFileDescriptor openParcelFileDescriptor() throws IOException;

}