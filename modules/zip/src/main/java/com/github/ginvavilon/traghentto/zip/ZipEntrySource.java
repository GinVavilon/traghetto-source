/**
 * 
 */
package com.github.ginvavilon.traghentto.zip;

import com.github.ginvavilon.traghentto.PathUtils;
import com.github.ginvavilon.traghentto.StreamSource;

/**
 * @author Vladimir Baraznovsky
 *
 */
public abstract class ZipEntrySource implements StreamSource {

    protected ZipSource mZipParent;

    public ZipEntrySource(ZipSource pZipParent) {
        super();
        mZipParent = pZipParent;
    }

    public ZipSource getZipParent() {
        return mZipParent;
    }

    public void setZipParent(ZipSource pZipParent) {
        mZipParent = pZipParent;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public String getName() {
        String name = getPath();
        return PathUtils.extractName(name);
    }

    @Override
    public String getUriString() {
        return mZipParent.getURI(this);
    }

}