/**
 * 
 */
package com.github.ginvavilon.traghentto.android.creators;

import android.net.Uri;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceCreator;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class PathAndroidCreator extends ProxyAndroidCreator {

    public PathAndroidCreator(SourceCreator<? extends Source> creator) {
        super(creator);
    }

    @Override
    protected String getParameter(Uri pUri) {
        return pUri.getPath();
    }

}
