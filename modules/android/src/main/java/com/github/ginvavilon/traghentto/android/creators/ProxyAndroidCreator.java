/**
 * 
 */
package com.github.ginvavilon.traghentto.android.creators;

import android.content.Context;
import android.net.Uri;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceCreator;

/**
 * @author Vladimir Baraznovsky
 *
 */
public abstract class ProxyAndroidCreator implements AndroidSourceCreator<Source> {

    protected final SourceCreator<? extends Source> mCreator;

    protected abstract String getParameter(Uri pUri);

    public ProxyAndroidCreator(SourceCreator<? extends Source> pCreator) {
        super();
        mCreator = pCreator;
    }

    @Override
    public Source create(Context pContext, Uri pUri) {
    
        Source source = mCreator.create(getParameter(pUri));
        return source;
    }

}