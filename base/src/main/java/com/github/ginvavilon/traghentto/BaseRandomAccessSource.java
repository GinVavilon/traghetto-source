/**
 * 
 */
package com.github.ginvavilon.traghentto;

import java.io.IOException;
import java.util.List;

/**
 * @author vbaraznovsky
 *
 */
class BaseRandomAccessSource<T extends Source> extends DelegatedSource<T> {

    private StreamSource mStreamSource;


    public BaseRandomAccessSource(StreamSource streamSource, T source) {
        super(source);
        mStreamSource = streamSource;
    }

    @Override
    public List<? extends Source> getChildren() {
        List<? extends Source> children = null;
        try {
            openStream();
            children = mSource.getChildren();
        } catch (IOException e) {
            Logger.e(e);
        } finally {
            StreamUtils.close(mStreamSource);
        }
        return children;
    }

    protected void openStream() throws IOException {
        mStreamSource.open();
    }

    protected void closeStream() {
        StreamUtils.close(mStreamSource);
    }

    protected boolean isStreamOpened() {

        return mStreamSource.isOpened();
    }

    @Override
    public Source getChild(String pName) {
        Source child = null;
        try {
            openStream();
            child = mSource.getChild(pName);
        } catch (IOException e) {
            Logger.e(e);
        } finally {
            closeStream();
        }
        if ((child != null) && (child instanceof StreamSource)) {
            return new ChildRandomAceessSource(mStreamSource, child);
        }
        return child;
    }

    @Override
    public long getLenght() {
        try {
            openStream();
            return mSource.getLenght();
        } catch (IOException e) {
            Logger.e(e);
        } finally {
            closeStream();
        }
        return UNKNOWN_LENGHT;
    }


    @Override
    public String toString() {
        return getUriString().toString();
    }


    @Override
    public boolean isDataAvailable() {
        return true;
    }

}