/**
 *
 */
package com.github.ginvavilon.traghentto.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.github.ginvavilon.traghentto.DeletableSource;
import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.SourceCreator;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.StreamSource;
import com.github.ginvavilon.traghentto.StreamUtils;
import com.github.ginvavilon.traghentto.URIBuilder;
import com.github.ginvavilon.traghentto.UriConstants;
import com.github.ginvavilon.traghentto.WritableSource;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class ZipFileSource extends BaseZipSouce implements StreamSource,DeletableSource{
    ZipFile mZipFile;
    private File mFile;

    public ZipFileSource(File pFile) {
	super();
	mFile = pFile;
    }

    @Override
    public ZipEntrySource getChild(String pName) {
	ZipEntry entry = mZipFile.getEntry(pName);
	if (entry != null) {
	    return new ExistZipEntrySource(entry, this);
	}
        return new NotExistZipEntrySource(this, pName);
    }

    protected List<ExistZipEntrySource> getChildren(String regExp) {
	Enumeration<? extends ZipEntry> entries = mZipFile.entries();
	List<ExistZipEntrySource> list = new ArrayList<ExistZipEntrySource>();
	while (entries.hasMoreElements()) {
	    ZipEntry entry = entries.nextElement();

	    if (entry.getName().matches(regExp)) {
		ExistZipEntrySource source = new ExistZipEntrySource(entry, this);
		list.add(source);
	    }

	}
	return list;
    }

    @Override
    public StreamResource<InputStream> openResource(StreamParams pParams)
            throws IOSourceException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPath() {
        return mFile.getAbsolutePath();
    }

    @Override
    public String getName() {
	return mZipFile.getName();
    }

    @Override
    public void close() throws IOException {
	mZipFile.close();
	mZipFile = null;
    }

    @Override
    public void open() throws IOException {
	mZipFile = new ZipFile(mFile);
    }

    @Override
    public boolean isOpened() {
	return mZipFile != null;
    }

    @Override
    public void closeEntry(ZipEntry pZipEntry) {
	try {
	    close();
	} catch (IOException e) {
	    Logger.e(e);
	}
    }

    @Override
    public InputStream openInputStream(ZipEntry pZipEntry) throws IOException {
	if (!isOpened()){
	    open();
	}
	return mZipFile.getInputStream(pZipEntry);
    }

    @Override
    public void openEntry(ZipEntry pZipEntry) throws IOException {
	open();
    }

    @Override
    public boolean exists() {
	return mFile.exists();
    }

    @Override
    public String getUriString() {
        URIBuilder builder = new URIBuilder();
	builder.scheme(UriConstants.ZIP_FILE_SCHEME);
	builder.authority(UriConstants.EMPTY_AUTHOTITY);
	builder.path(getPath());
        return builder.build().toString();

    }

    @Override
    public String getURI(ZipEntrySource pZipEntrySource) {
        URIBuilder builder = new URIBuilder();
	builder.scheme(UriConstants.ZIP_FILE_SCHEME);
	builder.authority(UriConstants.EMPTY_AUTHOTITY);
	builder.path(getPath());
	builder.encodedFragment(pZipEntrySource.getPath());
        return builder.build().toString();
    }

    @Override
    public long getLenght() {
	return mZipFile.size();
    }

    @Override
    public boolean isLocal() {
	return false;
    }

    @Override
    public boolean isDataAvailable() {
	return isOpened();
    }

//    @Override
//    public boolean unzip(IWritableSource pTo) throws IOException {
//	try {
//	    open();
//	    pTo.createConteiner();
//	    Enumeration<? extends ZipEntry> entries = mZipFile.entries();
//	    while (entries.hasMoreElements()) {
//		ZipEntry entry = entries.nextElement();
//		IWritableSource child = pTo.getChild(entry.getName());
//		if (entry.isDirectory()) {
//		    child.createConteiner();
//		} else {
//		    InputStream inputStream = null;
//		    OutputStream outputStream = null;
//		    try {
//			inputStream = mZipFile.getInputStream(entry);
//			child.create();
//			outputStream = child.openOutputStream();
//			StreamUtils.copyStream(inputStream, outputStream, null);
//		    }
//
//		    finally {
//			StreamUtils.close(inputStream);
//			SourceUtils.closeStream(child, outputStream);
//		    }
//		}
//
//	    }
//	    return true;
//	} finally {
//	    close();
//	}
//
//    }
    @Override
    public boolean unzip(WritableSource pTo) throws IOException {
        FileInputStream fileInputStream=null;

        ZipInputStream inputStream=null;

        try {
            fileInputStream = new FileInputStream(mFile);
            inputStream = new ZipInputStream(fileInputStream);
            pTo.createConteiner();
            ZipEntry entry = inputStream.getNextEntry();
            while ((entry != null)) {
                WritableSource child = pTo.getChild(entry.getName());
                if (entry.isDirectory()) {
                    child.createConteiner();
                } else {
                    StreamResource<OutputStream> outputResource = null;
                    try {
                        child.create();
                        outputResource = child.openOutputResource();
                        OutputStream outputStream = outputResource.getStream();
                        StreamUtils.copyStream(inputStream, outputStream,false, null);
                    } catch (Exception e) {
                            Logger.e(e);
                    } finally {
                        StreamUtils.close(outputResource);
                    }
                }
                entry = inputStream.getNextEntry();
            }

            return true;
        } catch (Exception e) {
             Logger.e(e);

        } finally {
            StreamUtils.close(inputStream);
            StreamUtils.close(fileInputStream);
        }
        return false;
    }


    @Override
    public boolean delete() {
	return mFile.delete();
    }

    public static final SourceCreator<ZipFileSource> CREATOR = new SourceCreator<ZipFileSource>() {

        @Override
        public ZipFileSource create(String pParam) {
            return new ZipFileSource(new File(pParam));
        }
    };

}
