/**
 *
 */
package com.github.ginvavilon.traghentto.android;

import android.content.Context;

import java.io.IOException;

import com.github.ginvavilon.traghentto.file.CachedSource;
import com.github.ginvavilon.traghentto.file.DiskLruCache;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class AndroidCache {

    private static int sCacheVersion = 0;


    public static DiskLruCache createCacheForSources(Context pContext, String pName, long pSize)
            throws IOException {
        return DiskLruCache.open(CacheUtils.getDiskCacheDir(pContext, pName), getCacheVersion(),
                CachedSource.COUNT_INDEX, pSize);
    }

    public static int getCacheVersion() {
        return sCacheVersion;
    }

    public static void setCacheVersion(int cacheVersion) {
        sCacheVersion = cacheVersion;
    }

}
