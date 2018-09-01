/**
 *
 */
package com.github.ginvavilon.traghentto.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.github.ginvavilon.traghentto.DelegatedSource;
import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.Logger.Level;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.StreamUtils;
import com.github.ginvavilon.traghentto.StreamUtils.ICopyListener;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.file.DiskLruCache.Editor;
import com.github.ginvavilon.traghentto.file.DiskLruCache.Snapshot;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class CachedSource<T extends Source> extends DelegatedSource<T>
        implements Source, ICopyListener {
    public static final int COUNT_INDEX = 2;
    private static final int INDEX_STREAM = 0;
    private static final int INDEX_SIZE = 1;

    private DiskLruCache mDiskLruCache;

    public CachedSource(DiskLruCache pDiskLruCache, T pSource) {
        super(pSource);
	mDiskLruCache = pDiskLruCache;

    }

    @Override
    public StreamResource<InputStream> openResource(StreamParams pParams)
            throws IOSourceException, IOException {
        String key = getKey();
        Snapshot snapshot = mDiskLruCache.get(key);
        if (snapshot != null) {
            StreamUtils.close(snapshot.getInputStream(INDEX_SIZE));
            return StreamUtils.createResource(snapshot.getInputStream(INDEX_STREAM));
        } else {
            Editor editor = mDiskLruCache.edit(key);
            if (editor != null) {
                OutputStream out = editor.newOutputStream(INDEX_STREAM);
                StreamResource<InputStream> inResource = mSource.openResource(pParams);
                InputStream in = inResource.getStream();
                try {
                    long size = StreamUtils.copyStream(in, out, false, true, this);
                    if (size > 0) {
                        editor.set(INDEX_SIZE, String.valueOf(size));
                        editor.commit();
                        mDiskLruCache.flush();
                        Snapshot value = mDiskLruCache.get(key);
                        if (value != null) {
                            return StreamUtils.createResource(value.getInputStream(INDEX_STREAM));
                        }
                    } else {
                        editor.abort();
                    }
                } finally {
                    StreamUtils.close(inResource);
                }
            }
        }

        return mSource.openResource(pParams);
    }


    private String getKey() {
	String key = FileUtils.hashKeyForDisk(mSource.getUriString().toString());
	return key;
    }


    @Override
    public long getLenght() {

        Snapshot snapshot = null;
	try {
            snapshot = mDiskLruCache.get(getKey());
	    if (snapshot != null) {
		String string = snapshot.getString(INDEX_SIZE);
		if (string == null) {
		    return Long.parseLong(string);
		}
	    }
	} catch (Exception e) {
            Logger.e(e);
        } finally {
            StreamUtils.close(snapshot);
	}
	return mSource.getLenght();
    }

    @Override
    public void onStart() {
        Logger.d(Level.SOURCE | Level.CACHE, "Start put into cache %s", mSource.getUriString());

    }

    @Override
    public void onProgress(long pRadedByte) {
//	long lenght = mSource.getLenght();
//	if (lenght>0){
//	    Log.d("Progeress copy %s%%(%s / %s)",(pRadedByte*100)/lenght, pRadedByte,lenght);
//	}
    }

    @Override
    public void onFail(Throwable pE) {
        Logger.e("Fail put into cache %s", pE, mSource.getUriString());
    }

    @Override
    public void onCompite() {
        Logger.d(Level.CACHE | Level.SOURCE, "Finish put into cache %s", mSource.getUriString());
    }

    @Override
    public String toString() {
        return getUriString().toString();
    }

    @Override
    public boolean isLocal() {
	return true;
    }
    @Override
    public boolean isDataAvailable() {
        return mDiskLruCache.has(getKey());
    }

    public boolean removeFromCache() throws IOException{
        return mDiskLruCache.remove(getKey());
    }

}
