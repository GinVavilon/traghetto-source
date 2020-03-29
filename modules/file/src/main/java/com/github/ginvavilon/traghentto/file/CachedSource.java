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
                StreamResource<InputStream> inResource = getSource().openResource(pParams);
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

        return getSource().openResource(pParams);
    }


    private String getKey() {
	String key = FileUtils.hashKeyForDisk(getSource().getUriString());
	return key;
    }


    @Override
    public long getLength() {

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
	return getSource().getLength();
    }

    @Override
    public void onStart() {
        Logger.d(Level.SOURCE | Level.CACHE, "Start put into cache %s", getSource().getUriString());

    }

    @Override
    public void onProgress(long pReadBytes) {
//	long length = mSource.getLength();
//	if (length>0){
//	    Log.d("Progress copy %s%%(%s / %s)",(pReadBytes*100)/length, pReadBytes,length);
//	}
    }

    @Override
    public void onFail(Throwable pE) {
        Logger.e("Fail put into cache %s", pE, getSource().getUriString());
    }

    @Override
    public void onComplete() {
        Logger.d(Level.CACHE | Level.SOURCE, "Finish put into cache %s", getSource().getUriString());
    }

    @Override
    public String toString() {
        return getUriString();
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
