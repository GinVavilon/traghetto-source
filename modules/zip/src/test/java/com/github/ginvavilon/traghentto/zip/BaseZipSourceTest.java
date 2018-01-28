/**
 * 
 */
package com.github.ginvavilon.traghentto.zip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.file.BaseSourceTest;

/**
 * @author vbaraznovsky
 *
 */
public abstract class BaseZipSourceTest<T extends Source> extends BaseSourceTest<T, Source> {

    public BaseZipSourceTest() {
        super();
    }

    public abstract T wrapSource(ZipFileSource zipFileSource);

    @Override
    protected void assertDataAvailable(boolean dataAvailable) {
        assertTrue(dataAvailable);
    }

    @Override
    protected void assertLocal(boolean local) {
        assertFalse(local);
    }

    @Override
    protected T getRootSource() {
        ZipFileSource zipFileSource = createRootZipSource();
        return wrapSource(zipFileSource);
    }

    protected ZipFileSource createRootZipSource() {
        File file = getResourceFile("test.zip");
        ZipFileSource zipFileSource = new ZipFileSource(file);
        return zipFileSource;
    }

    @Override
    protected boolean checkClosedStream(StreamResource<InputStream> singleResource) {
        return !singleResource.isOpened();
    }

    @Override
    public void assertChild(Source child, String childName) {
        String path = getTestDirectory().getPath();
        assertEquals(path + childName, child.getPath());

    }

}