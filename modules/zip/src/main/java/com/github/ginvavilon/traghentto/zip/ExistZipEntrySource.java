/**
 *
 */
package com.github.ginvavilon.traghentto.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceIterator;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.StreamSource;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
class ExistZipEntrySource extends ZipEntrySource implements StreamSource {

    private ZipEntry mZipEntry;
    public ExistZipEntrySource(ZipEntry pZipEntry, ZipSource pZipParent) {
        super(pZipParent);
	mZipEntry = pZipEntry;

    }


    @Override
    public List<? extends Source> getChildren() {
	return mZipParent.getChildren(mZipEntry);
    }


    @Override
    public Source getChild(String pName) {
	return mZipParent.getChild(this,pName);
    }

    @Override
    public boolean isContainer() {
	return mZipEntry.isDirectory();
    }

    @Override
    public StreamResource<InputStream> openResource(StreamParams pParams)
            throws IOSourceException, IOException {
        InputStream inputStream = mZipParent.openInputStream(mZipEntry);
        return StreamResource.createResource(inputStream);
    }

    @Override
    public String getPath() {
	return mZipEntry.getName();
    }

    @Override
    public void close() throws IOException {
	mZipParent.closeEntry(mZipEntry);
    }


    @Override
    public void open() throws IOException {
	mZipParent.openEntry(mZipEntry);
    }


    @Override
    public boolean isOpened() {
	return true;
    }

    @Override
    public boolean exists() {
	return true;
    }

    @Override
    public long getLength() {
	return mZipEntry.getSize();
    }


    @Override
    public boolean isDataAvailable() {
	return isOpened();
    }

    @Override
    public SourceIterator iterator() {
        return null;
    }
}
