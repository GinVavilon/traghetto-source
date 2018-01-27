/**
 *
 */
package com.github.ginvavilon.traghentto.zip;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.github.ginvavilon.traghentto.StreamSource;
import com.github.ginvavilon.traghentto.WritableSource;
import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.SourceUtils;
import com.github.ginvavilon.traghentto.StreamUtils;
import com.github.ginvavilon.traghentto.URIBuilder;
import com.github.ginvavilon.traghentto.UriConstants;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
@Deprecated
public class ZipStreamSource extends BaseZipSouce implements StreamSource {
    private static final String ARCHIVE_AUTORIRY = "archive";
    private ZipInputStream mZipInputStream;
    private InputStream mStream;
    private Map<String, ZipEntry> mEntries=null;

    public ZipStreamSource(InputStream pStream) {
	super();
	mStream =pStream;
    }

    @Override
    public void close() throws IOException {
	if (isOpened()) {
	   // mZipInputStream.close();
	    mStream.reset();
	    mZipInputStream = null;
	}
    }
    @Override
    public void open() throws IOException {
	if (isOpened()){
	    close();
	}
	mZipInputStream = new ZipInputStream(mStream);
	mStream.mark(mStream.available());
    }
    @Override
    public InputStream openInputStream(StreamParams pParams) throws IOException {
	return mZipInputStream;
    }

    @Override
    public String getPath() {
	return "stream://";
    }

    @Override
    public String getName() {
	return "[stream]";
    }



    @Override
    public boolean isOpened() {
	return mZipInputStream != null;
    }

    @Override
    public void closeEntry(ZipEntry pZipEntry) throws IOException {
	mZipInputStream.closeEntry();
	popStream();
    }

    @Override
    public InputStream openInputStream(ZipEntry pZipEntry) throws IOException {
	pushStream();
	ZipEntry entry = mZipInputStream.getNextEntry();
	while ((entry != null) && (!entry.getName().equals(pZipEntry.getName()))) {
	    entry = mZipInputStream.getNextEntry();
	}
	if (entry != null) {
	    return mZipInputStream;
	}

	return null;
    }

    @Override
    protected List<? extends ZipEntrySource> getChildren(String regExp) {


	List<ZipEntrySource> list = new ArrayList<ZipEntrySource>();
	try {
	    pushStream();
	    Collection<ZipEntry> values = getEntries().values();
	    for (ZipEntry entry : values) {
		if (entry.getName().matches(regExp)) {
		    ZipEntrySource source = new ZipEntrySource(entry, this);
		    list.add(source);
		}
	    }
	    popStream();
	} catch (IOException e) {
	    Logger.e(e);
	}
	return list;
    }

    /**
     * @return
     *
     */
    private Map<String, ZipEntry> getEntries() {
	if (mEntries==null){
	    mEntries=new HashMap<String, ZipEntry>();
	    ZipEntry entry;
	    try {
		while ((entry = mZipInputStream.getNextEntry()) != null) {
		mEntries.put(entry.getName(), entry);
		}
	    } catch (IOException e) {
		Logger.e(e);
	    }
	}
	return mEntries;

    }

    private void popStream() throws IOException {
	//mZipInputStream.reset();
    }

    private void pushStream() throws IOException {
	//mZipInputStream.mark(mZipInputStream.available());
    }

    @Override
    public ZipEntrySource getChild(String pName) {
	ZipEntry entry;
	entry = getEntries().get(pName);
	if (entry == null) {
	    entry = getEntries().get(pName + "/");
	}
	if (entry != null) {
	    return new ZipEntrySource(entry, this);
	}

	return null;
    }

    @Override
    public void openEntry(ZipEntry pZipEntry) throws IOException {
	open();
    }

    @Override
    public boolean exists() {
	return true;
    }

    @Override
    public void closeStream(Closeable pStream) throws IOException {
    }

    @Override
    public String getUriString() {
        URIBuilder builder = new URIBuilder();
	builder.scheme(UriConstants.STREAM_SCHEME);
	builder.authority(ARCHIVE_AUTORIRY);
        return builder.build().toString();

    }

    @Override
    public String getURI(ZipEntrySource pZipEntrySource) {
        URIBuilder builder = new URIBuilder();
	builder.scheme(UriConstants.STREAM_SCHEME);
	builder.authority(ARCHIVE_AUTORIRY);
	builder.encodedFragment(pZipEntrySource.getPath());
        return builder.build().toString();
    }

    @Override
    public long getLenght() {
	return UNKNOWN_LENGHT;
    }

    @Override
    public boolean isLocal() {
	return false;
    }

    @Override
    public boolean isDataAvailable() {
	return isOpened();
    }

    @Override
    public boolean unzip(WritableSource pTo) throws IOException {
	try {
	    ZipInputStream inputStream = new ZipInputStream(mStream);
	    pTo.createConteiner();
	    ZipEntry entry = inputStream.getNextEntry();
	    while ((entry != null)) {
		WritableSource child = pTo.getChild(entry.getName());
		if (entry.isDirectory()) {
		    child.createConteiner();
		} else {
		    OutputStream outputStream = null;
		    try {
			child.create();
			outputStream = child.openOutputStream();
			StreamUtils.copyStream(inputStream, outputStream,false,null);
		    } finally {
			SourceUtils.closeStream(child, outputStream);
		    }
		}
		entry = inputStream.getNextEntry();
	    }
	    StreamUtils.close(inputStream);
	    return true;
	} finally {
	    close();
	}

    }
}
