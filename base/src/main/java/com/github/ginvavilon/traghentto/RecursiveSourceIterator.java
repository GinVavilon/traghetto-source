/**
 * 
 */
package com.github.ginvavilon.traghentto;

import java.util.Iterator;

/**
 * @author vbaraznovsky
 *
 */
class RecursiveSourceIterator implements SourceIterator {

    private SourceIterator mCurrentIterator;

    private Iterator<? extends Source> mIterator;

    RecursiveSourceIterator(Source source) {
        mIterator = source.getChildren().iterator();
    }

    @Override
    public boolean hasNext() {
        if ((mCurrentIterator != null) && (mCurrentIterator.hasNext())) {
            return true;
        }

        return mIterator.hasNext();
    }

    @Override
    public Source next() {
        if ((mCurrentIterator != null) && (mCurrentIterator.hasNext())) {
            return mCurrentIterator.next();
        }
        if (mCurrentIterator != null) {
            try {
                mCurrentIterator.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            mCurrentIterator = null;
        }

        Source next = mIterator.next();
        if (next.isConteiner()) {
            mCurrentIterator = next.iterator();
        }
        return next;
    }

    @Override
    public void close() throws Exception {

    }

}
