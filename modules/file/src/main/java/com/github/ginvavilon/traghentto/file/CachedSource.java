/**
 *
 */
package com.github.ginvavilon.traghentto.file;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.github.ginvavilon.traghentto.ISource;
import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.StreamUtils;
import com.github.ginvavilon.traghentto.Logger.Level;
import com.github.ginvavilon.traghentto.StreamUtils.ICopyListener;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.file.DiskLruCache.Editor;
import com.github.ginvavilon.traghentto.file.DiskLruCache.Snapshot;
import com.github.ginvavilon.traghentto.params.ISourceStreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class CachedSource<T extends ISource> implements ISource, ICopyListener {
    protected static final int CACHE_VERSION = 0;
    public static final int COUNT_INDEX = 2;
    private static final int INDEX_STREAM = 0;
    private static final int INDEX_SIZE = 1;

    private final T mSource;
    private DiskLruCache mDiskLruCache;

    public CachedSource(DiskLruCache pDiskLruCache, T pSource) {
	super();
	mDiskLruCache = pDiskLruCache;
	mSource = pSource;

    }

    public List<? extends ISource> getChildren() {
	return mSource.getChildren();
    }

    public ISource getChild(String pName) {
	return mSource.getChild(pName);
    }

    public boolean isConteiner() {
	return mSource.isConteiner();
    }
    @Override
    public InputStream openInputStream(ISourceStreamParams pParams) throws IOException,
            IOSourceException {
	String key = getKey();
	Snapshot snapshot = mDiskLruCache.get(key);
	if (snapshot != null) {
            StreamUtils.close(snapshot.getInputStream(INDEX_SIZE));
	    return snapshot.getInputStream(INDEX_STREAM);
	} else {
	    Editor editor = mDiskLruCache.edit(key);
	    if (editor != null) {
		OutputStream out = editor.newOutputStream(INDEX_STREAM);
		InputStream in = mSource.openInputStream(pParams);
		long size = StreamUtils.copyStream(in, out, this);
		if (size>0) {
		    editor.set(INDEX_SIZE, String.valueOf(size));
		    editor.commit();
		    mDiskLruCache.flush();
		    Snapshot value = mDiskLruCache.get(key);
		    if (value != null) {
			return value.getInputStream(INDEX_STREAM);
		    }
		} else {
		    editor.abort();
		}
	    }
	}
	return mSource.openInputStream(pParams);
    }

    private String getKey() {
	String key = FileUtils.hashKeyForDisk(mSource.getUriString().toString());
	return key;
    }

    public void closeStream(Closeable pStream) throws IOException {
	mSource.closeStream(pStream);
    }

    public String getPath() {
	return mSource.getPath();
    }

    public String getName() {
	return mSource.getName();
    }

    public String getUriString() {
	return mSource.getUriString();
    }

    public boolean exists() {
	return mSource.exists();
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

    public T getSource() {
        return mSource;
    }
}
