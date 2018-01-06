/**
 *
 */
package com.github.ginvavilon.traghentto.android;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.util.List;

import com.github.ginvavilon.traghentto.file.CachedSource;
import com.github.ginvavilon.traghentto.file.DiskLruCache;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class AndroidCachedSource<T extends IAndroidSource> extends CachedSource<T> implements
        IAndroidSource {

    public AndroidCachedSource(DiskLruCache pDiskLruCache, T pSource) {
        super(pDiskLruCache, pSource);
    }

    public IAndroidSource getChild(String name) {
        return null;
    }

    public List<? extends IAndroidSource> getChildren() {
        return null;
    }

    @Override
    public Uri getUri() {
        return null;
    }

    @Override
    public ParcelFileDescriptor openParcelFileDescriptor() throws IOException {
        return null;
    }

    public static DiskLruCache createCacheForSources(Context pContext, String pName, long pSize) throws IOException{
        return DiskLruCache.open(CacheUtils.getDiskCacheDir(pContext, pName),CACHE_VERSION,CachedSource.COUNT_INDEX,  pSize);
    }

}
