/**
 * 
 */
package com.github.ginvavilon.traghentto.zip;

import com.github.ginvavilon.traghentto.StreamSource;

/**
 * @author vbaraznovsky
 *
 */
public abstract class ZipEntrySource implements StreamSource {

    protected ZipSource mZipParrent;

    public ZipEntrySource(ZipSource pZipParrent) {
        super();
        mZipParrent = pZipParrent;
    }

    public ZipSource getZipParrent() {
        return mZipParrent;
    }

    public void setZipParrent(ZipSource pZipParrent) {
        mZipParrent = pZipParrent;
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
        return mZipParrent.getURI(this);
    }

}