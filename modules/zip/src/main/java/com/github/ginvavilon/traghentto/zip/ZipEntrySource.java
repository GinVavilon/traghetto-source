/**
 * 
 */
package com.github.ginvavilon.traghentto.zip;

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
        int last = name.length()-1;
        if ('/'==(name.charAt(last))){
            last--;
        }
        int index = name.lastIndexOf("/",last);
        return name.substring(index+1,last+1);
    }

    @Override
    public String getUriString() {
        return mZipParent.getURI(this);
    }

}