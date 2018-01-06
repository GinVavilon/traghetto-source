/**
 *
 */
package com.github.ginvavilon.traghentto.android;

import com.github.ginvavilon.traghentto.IWritableSource;

/**
 * @author Vladimir Baraznovsky
 *
 */
public interface IAndroidWritableSource extends IWritableSource, IAndroidSource {
    IAndroidWritableSource getChild(String name);

}
