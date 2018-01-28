/**
 * 
 */
package com.github.ginvavilon.traghentto.se;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.github.ginvavilon.traghentto.SourceUtils;
import com.github.ginvavilon.traghentto.file.FileSource;
import com.github.ginvavilon.traghentto.zip.ZipRandomAccessFileSourceTest;

/**
 * @author vbaraznovsky
 *
 */
public class UnZipTest extends ZipRandomAccessFileSourceTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void unzipTest() throws Exception {
        File toFile = mTempTestFolder.newFolder("test_out");
        FileSource toSource = new FileSource(toFile);
        SourceUtils.copy(getRootSource(), toSource);
        assertStructure(toSource);
    }


}
