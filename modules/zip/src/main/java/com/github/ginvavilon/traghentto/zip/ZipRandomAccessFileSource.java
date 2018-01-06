/**
 *
 */
package com.github.ginvavilon.traghentto.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;

import com.github.ginvavilon.traghentto.IWritableSource;
import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.RandomAccessFileSource;
import com.github.ginvavilon.traghentto.SourceCreator;
import com.github.ginvavilon.traghentto.StreamUtils;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class ZipRandomAccessFileSource extends RandomAccessFileSource implements IZipArhive {

    private IZipArhive mSource;

    public ZipRandomAccessFileSource(IZipArhive pSource) {
        super(pSource);
        mSource = pSource;
    }

    public void close() throws IOException {
        mSource.close();
    }

    public void open() throws IOException {
        mSource.open();
    }

    public boolean isOpened() {
        return true;
    }

    public List<? extends ZipEntrySource> getChildren() {
        try {
            mSource.open();
            List<? extends ZipEntrySource> children = mSource.getChildren();

            for (ZipEntrySource zipEntrySource : children) {
                zipEntrySource.setZipParrent(this);
            }
            return children;
        } catch (IOException e) {
            Logger.e(e);
        } finally {
            StreamUtils.close(mSource);
        }
        return new LinkedList<>();

    }

    public void openEntry(ZipEntry pZipEntry) throws IOException {
        mSource.openEntry(pZipEntry);
    }

    public void closeEntry(ZipEntry pZipEntry) throws IOException {
        mSource.closeEntry(pZipEntry);
    }

    public InputStream openInputStream(ZipEntry pZipEntry) throws IOException {
        return mSource.openInputStream(pZipEntry);
    }

    public ZipEntrySource getChild(ZipEntrySource pZipEntrySource, String pName) {
        ZipEntrySource child = mSource.getChild(pZipEntrySource, pName);
        child.setZipParrent(this);
        return child;
    }

    public ZipEntrySource getChild(String pName) {
        ZipEntrySource child = null;
        try {
            mSource.open();
            child = mSource.getChild(pName);
            child.setZipParrent(this);
        } catch (IOException e) {
            Logger.e(e);
        } finally {
            StreamUtils.close(mSource);
        }
        return child;

    }

    @Override
    public List<? extends ZipEntrySource> getChildren(ZipEntry pZipEntry) {
        List<? extends ZipEntrySource> children = null;
        try {
            mSource.open();
            children = mSource.getChildren(pZipEntry);
            for (ZipEntrySource child : children) {
                child.setZipParrent(this);
            }
        } catch (IOException e) {
            Logger.e(e);
        } finally {
            StreamUtils.close(mSource);
        }
        return children;
    }

    @Override
    public String getURI(ZipEntrySource pZipEntrySource) {
        return mSource.getURI(pZipEntrySource);
    }

    @Override
    public boolean unzip(IWritableSource pTo) throws IOException {
        return mSource.unzip(pTo);
    }

    public static SourceCreator<ZipRandomAccessFileSource> CREATOR = new SourceCreator<ZipRandomAccessFileSource>() {

        @Override
        public ZipRandomAccessFileSource create(String pParam) {
            return new ZipRandomAccessFileSource(ZipFileSource.CREATOR.create(pParam));
        }
    };

}
