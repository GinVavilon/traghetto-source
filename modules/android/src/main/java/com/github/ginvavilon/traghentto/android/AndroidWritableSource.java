/**
 *
 */
package com.github.ginvavilon.traghentto.android;

import com.github.ginvavilon.traghentto.WritableSource;

/**
 * @author Vladimir Baraznovsky
 *
 */
public interface AndroidWritableSource extends WritableSource, AndroidSource {
    AndroidWritableSource getChild(String name);

}
