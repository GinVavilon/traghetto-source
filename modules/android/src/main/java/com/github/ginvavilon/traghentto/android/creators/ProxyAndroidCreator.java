/**
 * 
 */
package com.github.ginvavilon.traghentto.android.creators;

import android.content.Context;
import android.net.Uri;

import com.github.ginvavilon.traghentto.ISource;
import com.github.ginvavilon.traghentto.SourceCreator;

/**
 * @author vbaraznovsky
 *
 */
public abstract class ProxyAndroidCreator implements AndroidSourceCreator<AndroidProxySource> {

    protected final SourceCreator<? extends ISource> mCreator;

    protected abstract String getParameter(Uri pUri);

    public ProxyAndroidCreator(SourceCreator<? extends ISource> pCreator) {
        super();
        mCreator = pCreator;
    }

    @Override
    public AndroidProxySource create(Context pContext, Uri pUri) {
    
        ISource source = mCreator.create(getParameter(pUri));
        return new AndroidProxySource(source);
    }

}