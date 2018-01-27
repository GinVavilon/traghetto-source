/**
 *
 */
package com.github.ginvavilon.traghentto.zip;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.StreamSource;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
class ZipEntrySource implements StreamSource {

    private ZipEntry mZipEntry;
    private ZipSource mZipParrent;

    public ZipEntrySource(ZipEntry pZipEntry, ZipSource pZipParrent) {
	super();
	mZipEntry = pZipEntry;
	mZipParrent = pZipParrent;
    }


    @Override
    public List<? extends Source> getChildren() {
	return mZipParrent.getChildren(mZipEntry);
    }


    @Override
    public Source getChild(String pName) {
	return mZipParrent.getChild(this,pName);
    }

    @Override
    public boolean isConteiner() {
	return mZipEntry.isDirectory();
    }


    @Override
    public InputStream openInputStream(StreamParams pParams) throws IOException {
	return mZipParrent.openInputStream(mZipEntry);
    }


    @Override
    public String getPath() {
	return mZipEntry.getName();
    }

    @Override
    public String getName() {
	String name = mZipEntry.getName();
	int last = name.length()-1;
	if ('/'==(name.charAt(last))){
	    last--;
	}
	int index = name.lastIndexOf("/",last);
	return name.substring(index+1,last+1);
    }



    @Override
    public void close() throws IOException {
	mZipParrent.closeEntry(mZipEntry);
    }


    @Override
    public void open() throws IOException {
	mZipParrent.openEntry(mZipEntry);
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
    public void closeStream(Closeable pStream) throws IOException {
	mZipParrent.closeStream(pStream);

    }


    @Override
    public String getUriString() {
	return mZipParrent.getURI(this);
    }


    @Override
    public long getLenght() {
	return mZipEntry.getSize();
    }


    public ZipSource getZipParrent() {
	return mZipParrent;
    }


    public void setZipParrent(ZipSource pZipParrent) {
	mZipParrent = pZipParrent;
    }

    @Override
    public boolean isLocal() {
	return false;
    }


    @Override
    public boolean isDataAvailable() {
	return isOpened();
    }
}
