/**
 * 
 */
package com.github.ginvavilon.traghentto.android.creators;

import android.content.Context;
import android.net.Uri;

import com.github.ginvavilon.traghentto.Source;

/**
 * @author Vladimir Baraznovsky
 *
 */
public interface AndroidSourceCreator<T extends Source> {
    T create(Context context, Uri uri);
}
