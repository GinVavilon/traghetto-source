/**
 *
 */
package com.github.ginvavilon.traghentto.zip;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;

import com.github.ginvavilon.traghentto.ISource;
import com.github.ginvavilon.traghentto.IStreamSource;
import com.github.ginvavilon.traghentto.params.ISourceStreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
class ZipEntrySource implements IStreamSource {

    private ZipEntry mZipEntry;
    private IZipArhive mZipParrent;

    public ZipEntrySource(ZipEntry pZipEntry, IZipArhive pZipParrent) {
	super();
	mZipEntry = pZipEntry;
	mZipParrent = pZipParrent;
    }


    @Override
    public List<? extends ISource> getChildren() {
	return mZipParrent.getChildren(mZipEntry);
    }


    @Override
    public ISource getChild(String pName) {
	return mZipParrent.getChild(this,pName);
    }

    @Override
    public boolean isConteiner() {
	return mZipEntry.isDirectory();
    }


    @Override
    public InputStream openInputStream(ISourceStreamParams pParams) throws IOException {
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


    public IZipArhive getZipParrent() {
	return mZipParrent;
    }


    public void setZipParrent(IZipArhive pZipParrent) {
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
