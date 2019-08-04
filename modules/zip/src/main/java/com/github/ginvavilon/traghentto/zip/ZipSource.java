/**
 *
 */
package com.github.ginvavilon.traghentto.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;

import com.github.ginvavilon.traghentto.StreamSource;
import com.github.ginvavilon.traghentto.WritableSource;

/**
 * @author Vladimir Baraznovsky
 *
 */
public interface ZipSource  extends StreamSource{
    void openEntry(ZipEntry pZipEntry) throws IOException;
    void closeEntry(ZipEntry pZipEntry) throws IOException;
    InputStream openInputStream(ZipEntry pZipEntry) throws IOException;

    List<? extends ZipEntrySource> getChildren(ZipEntry pZipEntry);

    List<? extends ZipEntrySource> getChildren();

    ZipEntrySource getChild(ZipEntrySource pZipEntrySource, String pName);

    ZipEntrySource getChild(String pName);

    String getURI(ZipEntrySource pZipEntrySource);
    boolean unzip(WritableSource pTo) throws IOException;
}
