/**
 *
 */
package com.github.ginvavilon.traghentto;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class RandomAccessFileSource implements Source {

    private StreamSource mSource;

    public RandomAccessFileSource(StreamSource pSource) {
	super();
	mSource = pSource;
    }

    @Override
    public List<? extends Source> getChildren() {
	List<? extends Source> children = null;
	try {
	    mSource.open();
	    children = mSource.getChildren();
	} catch (IOException e) {
	    Logger.e(e);
	} finally {
	    StreamUtils.close(mSource);
	}
	return children;
    }

    @Override
    public Source getChild(String pName) {
	Source child = null;
	try {
	    mSource.open();
	    child = mSource.getChild(pName);
	} catch (IOException e) {
	    Logger.e(e);
	} finally {
	    StreamUtils.close(mSource);
	}
	if ((child != null) && (child instanceof StreamSource)) {
	    return new RandomAccessFileSource((StreamSource) child);
	}
	return child;
    }

    @Override
    public boolean isConteiner() {
	return mSource.isConteiner();

    }

    @Override
    public InputStream openInputStream(StreamParams pParams) throws IOException,
            IOSourceException {
	mSource.open();
	return mSource.openInputStream(pParams);
    }

    @Override
    public String getPath() {
	return mSource.getPath();
    }

    @Override
    public String getName() {
	return mSource.getName();
    }

    @Override
    public boolean exists() {
	return mSource.exists();
    }

    @Override
    public void closeStream(Closeable pStream) throws IOException {
	try {
	    mSource.closeStream(pStream);
	} finally {
	    StreamUtils.close(mSource);
	}

    }

    @Override
    public String getUriString() {
	return mSource.getUriString();
    }

    @Override
    public long getLenght() {
	try {
	    mSource.open();
	    return mSource.getLenght();
	} catch (IOException e) {
	    Logger.e(e);
	} finally{
	    StreamUtils.close(mSource);
	}
	return UNKNOWN_LENGHT;
    }

    @Override
    public String toString() {
        return getUriString().toString();
    }

    @Override
    public boolean isLocal() {
	return mSource.isLocal();
    }

    @Override
    public boolean isDataAvailable() {
	return true;
    }
}
