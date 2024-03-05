/**
 * 
 */
package com.github.ginvavilon.traghentto.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.ginvavilon.traghentto.BaseWritableSource;
import com.github.ginvavilon.traghentto.IOSourceUtils;
import com.github.ginvavilon.traghentto.SourceUtils;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.UriConstants;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class TestFileSource extends BaseSourceTest<FileSource, FileSource> {


    private static final String TEST_ASSETS = "test_assets/";



    private FileSource mSingleFileSource;
    private String mAbsoluteSinglePath;
    private FileSource mDirectorySource;
    private File mDirectory;


    @Before
    public void setUp() throws Exception {
        File root = getResourceFile(TEST_ASSETS);
        File file = new File(root, TEST_FILE);

        mDirectory = new File(root, TEST_DIRECTORY);
        mAbsoluteSinglePath = file.getAbsolutePath();
        mSingleFileSource = new FileSource(file);
        mDirectorySource = getResourceFileSource(TEST_DIRECTORY);
    }

    public FileSource getResourceFileSource(String name) {
        return new FileSource(getResourceFile(TEST_ASSETS + name));
    }

    @Override
    protected FileSource getRootSource() {
        return getResourceFileSource("");
    }

    @After
    public void tearDown() throws Exception {

    }


    @Override
    public void assertChild(FileSource child, String childName) {
        File file = new File(mDirectory, childName);
        assertEquals(file, child.getFile());
    }

    @Override
    public FileSource getTestDirectory() {
        return mDirectorySource;
    }

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#getPath()}.
     */
    @Test
    public void testGetPath() {
        assertEquals(mAbsoluteSinglePath, getTestFile().getPath());
    }

    @Override
    public FileSource getTestFile() {
        return mSingleFileSource;
    }

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#getUriString()}.
     */
    @Test
    public void testGetUriString() {
        assertEquals(UriConstants.FILE_SCHEME + ":" + mAbsoluteSinglePath,
                getTestFile().getUriString());
    }

    @Override
    protected void assertLocal(boolean local) {
        assertTrue(local);
    }

    @Override
    protected void assertDataAvailable(boolean dataAvailable) {
        assertTrue(dataAvailable);
    }

    /**
     * Test method for
     * {@link com.github.ginvavilon.traghentto.file.FileSource#createContainer()}.
     * 
     * @throws IOException
     */
    @Test
    public void testCreateContainer() throws IOException {
        File root = mTempTestFolder.newFolder();
        FileSource rootSource = new FileSource(root);
        rootSource.getChild(TEST_CHILD_FOLDER).createContainer();

        File file = new File(root, TEST_CHILD_FOLDER);
        assertTrue("Container does not exits", file.exists());
        assertTrue("File is not directory", file.isDirectory());
    }

    /**
     * Test method for
     * {@link com.github.ginvavilon.traghentto.file.FileSource#create()}.
     * 
     * @throws IOException
     */
    @Test
    public void testCreate() throws IOException {
        File root = mTempTestFolder.newFolder();
        File dir = new File(root, TEST_CHILD_FOLDER);
        File file = new File(dir, TEST_CHILD);
        FileSource fileSource = new FileSource(file);
        assertFalse(file.exists());
        fileSource.create();
        assertTrue(file.exists());

    }

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#getFile()}.
     */
    @Test
    public void testGetFile() {
        assertEquals(new File(mAbsoluteSinglePath), mSingleFileSource.getFile());
    }

    /**
     * Test method for
     * {@link com.github.ginvavilon.traghentto.file.FileSource#delete()}.
     * 
     * @throws IOException
     */
    @Test
    public void testDelete() throws IOException {
        File root = mTempTestFolder.newFolder();
        File file = new File(root, TEST_CHILD);
        file.createNewFile();
        assertDelete(file);

        File dir = new File(root, TEST_DIRECTORY);
        dir.mkdir();
        file = new File(dir, TEST_CHILD_FOLDER);
        file.mkdir();

        for (String child : TEST_CHILD_FILES) {
            new File(file, child).createNewFile();
        }
        for (String child : TEST_CHILD_FILES) {
            new File(dir, child).createNewFile();
        }

        assertDelete(dir);

    }

    @Test
    public void testCopy() throws Exception {
        File root = mTempTestFolder.newFolder();
        FileSource to = new FileSource(root);
        SourceUtils.copy(mRoot, to);
        assertStructure(to);
    }

    public void assertDelete(File file) {
        assertTrue(file.exists());
        FileSource fileSource = new FileSource(file);
        fileSource.delete();
        assertFalse(file.exists());
    }


    /**
     * Test method for
     * {@link BaseWritableSource#openOutputResource()}.
     * 
     * @throws IOException
     * @throws IOSourceException
     */
    @Test
    public void testOpenOutputResource() throws IOException, IOSourceException {
        File root = mTempTestFolder.newFolder();
        File file = new File(root, TEST_CHILD);
        FileSource fileSource = new FileSource(file);
        assertFalse(file.exists());
        fileSource.create();
        assertTrue(file.exists());
        StreamResource<OutputStream> resource = fileSource.openOutputResource(null);
        OutputStream stream = resource.getStream();

        stream.write(TEST_TEXT.getBytes());
        stream.flush();
        resource.close();
        boolean hasIOException = false;
        try {
            stream.write("Lestt".getBytes());
        } catch (IOException e) {
            hasIOException = true;
        }
        if (!hasIOException) {
            fail("Stream must be closed");
        }

        StreamResource<InputStream> singleResource = fileSource.openResource(null);
        String string = IOSourceUtils.readStringFromResource(singleResource);
        assertEquals(TEST_TEXT, string);
        
        hasIOException = false;
        try {
            singleResource.getStream().read();
        } catch (IOException e) {
            hasIOException = true;
        }
        if (!hasIOException) {
            fail("Stream must be closed");
        }

    }
}
