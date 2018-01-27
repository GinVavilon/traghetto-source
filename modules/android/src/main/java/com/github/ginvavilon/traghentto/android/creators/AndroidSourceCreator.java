/**
 * 
 */
package com.github.ginvavilon.traghentto.android.creators;

import android.content.Context;
import android.net.Uri;

import com.github.ginvavilon.traghentto.android.AndroidSource;

/**
 * @author vbaraznovsky
 *
 */
public interface AndroidSourceCreator<T extends AndroidSource> {
    T create(Context context, Uri uri);
}
