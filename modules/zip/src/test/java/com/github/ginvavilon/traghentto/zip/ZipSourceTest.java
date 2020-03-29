/**
 * 
 */
package com.github.ginvavilon.traghentto.zip;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class ZipSourceTest extends BaseZipSourceTest<ZipFileSource> {

    private ZipFileSource mZipFileSource;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void down() throws Exception {
        mZipFileSource.close();
    }

    @Override
    public ZipFileSource wrapSource(ZipFileSource zipFileSource) {
        mZipFileSource = zipFileSource;
        try {
            zipFileSource.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return zipFileSource;
    }
}
