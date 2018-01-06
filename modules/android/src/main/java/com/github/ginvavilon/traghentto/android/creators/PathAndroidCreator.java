/**
 * 
 */
package com.github.ginvavilon.traghentto.android.creators;

import android.net.Uri;

import com.github.ginvavilon.traghentto.ISource;
import com.github.ginvavilon.traghentto.SourceCreator;

/**
 * @author vbaraznovsky
 *
 */
public class PathAndroidCreator extends ProxyAndroidCreator {

    public PathAndroidCreator(SourceCreator<? extends ISource> creator) {
        super(creator);
    }

    @Override
    protected String getParameter(Uri pUri) {
        return pUri.getPath();
    }

}
