/**
 *
 */
package com.github.ginvavilon.traghentto.zip;

import java.util.List;
import java.util.zip.ZipEntry;

import com.github.ginvavilon.traghentto.Source;

/**
 * @author Vladimir Baraznovsky
 *
 */
public abstract class BaseZipSouce implements Source, ZipSource {
    static final String EXPRESSION_ROOT_FILE = "/?[^/]+/?";
    static final String EXPRESSION_CHILD_FILE = "[^/]+/?";

    @Override
    public List<? extends ZipEntrySource> getChildren() {
        List<? extends ZipEntrySource> list = getChildren(EXPRESSION_ROOT_FILE);
        return list;
    }



    @Override
    public List<? extends ZipEntrySource> getChildren(ZipEntry pZipEntry) {
        if (!pZipEntry.isDirectory()) {
            return null;
        }
        return getChildren(pZipEntry+EXPRESSION_CHILD_FILE);
    }

    protected abstract List<? extends ExistZipEntrySource> getChildren(String pString);

    @Override
    public boolean isConteiner() {
	return true;
    }


    @Override
    public ZipEntrySource getChild(ZipEntrySource pZipEntrySource, String pName) {
        return getChild(pZipEntrySource.getPath()+pName);
    }
    @Override
    public String toString() {
        return getUriString().toString();
    }

}