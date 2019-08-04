/**
 * 
 */
package com.github.ginvavilon.traghentto.path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.ginvavilon.traghentto.SourceUtils;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.StreamUtils;
import com.github.ginvavilon.traghentto.UriConstants;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.file.BaseSourceTest;

/**
 * @author vbaraznovsky
 *
 */
public class TestPathSource extends BaseSourceTest<PathSource, PathSource> {


    private static final String TEST_ASSETS = "test_assets/";



    private PathSource mSinglePathSource;
    private String mAbsoluteSinglePath;
    private PathSource mDirectorySource;
    private Path mDirectory;



    @Before
    public void setUp() throws Exception {
        File root = getResourceFile(TEST_ASSETS);
        File file = new File(root, TEST_FILE);

        mDirectory = new File(root, TEST_DIRECTORY).toPath();
        mAbsoluteSinglePath = file.getAbsolutePath();
        mSinglePathSource = new PathSource(file.toPath());
        mDirectorySource = getResourcePathSource(TEST_DIRECTORY);
    }

    public PathSource getResourcePathSource(String name) {
        return new PathSource(getResourceFile(TEST_ASSETS + name).toPath());
    }

    @Override
    protected PathSource getRootSource() {
        return getResourcePathSource("");
    }

    @After
    public void tearDown() throws Exception {

    }


    @Override
    public void assertChild(PathSource child, String childName) {
        Path path = mDirectory.resolve(childName);
        assertEquals(path, child.getNioPath());
    }

    @Override
    public PathSource getTestDirectory() {
        return mDirectorySource;
    }

    /**
     * Test method for
     * {@link com.github.ginvavilon.traghentto.path.PathSource#getPath()}.
     */
    @Test
    public void testGetPath() {
        assertEquals(mAbsoluteSinglePath, getTestFile().getPath());
    }

    @Override
    public PathSource getTestFile() {
        return mSinglePathSource;
    }

    /**
     * Test method for
     * {@link com.github.ginvavilon.traghentto.path.PathSource#getUriString()}.
     */
    @Test
    public void testGetUriString() {
        assertEquals(UriConstants.FILE_SCHEME + "://" + mAbsoluteSinglePath,
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
     * {@link com.github.ginvavilon.traghentto.path.PathSource#createConteiner()}.
     * 
     * @throws IOException
     */
    @Test
    public void testCreateConteiner() throws IOException {
        File root = mTempTestFolder.newFolder();
        PathSource rootSource = new PathSource(root);
        rootSource.getChild(TEST_CHILD_FOLDER).createConteiner();

        File file = new File(root, TEST_CHILD_FOLDER);
        assertTrue("Conteiner does not exits", file.exists());
        assertTrue("File is not directory", file.isDirectory());
    }

    /**
     * Test method for
     * {@link com.github.ginvavilon.traghentto.path.PathSource#create()}.
     * 
     * @throws IOException
     */
    @Test
    public void testCreate() throws IOException {
        File root = mTempTestFolder.newFolder();
        File dir = new File(root, TEST_CHILD_FOLDER);
        File file = new File(dir, TEST_CHILD);
        PathSource PathSource = new PathSource(file);
        assertFalse(file.exists());
        PathSource.create();
        assertTrue(file.exists());

    }

    /**
     * Test method for
     * {@link com.github.ginvavilon.traghentto.path.PathSource#getNioPath()}.
     */
    @Test
    public void testGetNioPath() {
        assertEquals(Paths.get(mAbsoluteSinglePath), mSinglePathSource.getNioPath());
    }

    /**
     * Test method for
     * {@link com.github.ginvavilon.traghentto.path.PathSource#delete()}.
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
        PathSource to = new PathSource(root);
        SourceUtils.copy(mRoot, to);
        assertStructure(to);
    }

    public void assertDelete(File file) {
        assertTrue(file.exists());
        PathSource PathSource = new PathSource(file);
        PathSource.delete();
        assertFalse(file.exists());
    }


    /**
     * Test method for
     * {@link com.github.ginvavilon.traghentto.BaseWritebleSource#openOutputResource()}.
     * 
     * @throws IOException
     * @throws IOSourceException
     */
    @Test
    public void testOpenOutputResource() throws IOException, IOSourceException {
        File root = mTempTestFolder.newFolder();
        File file = new File(root, TEST_CHILD);
        PathSource PathSource = new PathSource(file.toPath());
        assertFalse(file.exists());
        PathSource.create();
        assertTrue(file.exists());
        StreamResource<OutputStream> resource = PathSource.openOutputResource(null);
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
            fail("Streem must be closed");
        }

        StreamResource<InputStream> singleResource = PathSource.openResource(null);
        String string = StreamUtils.readStringFromResource(singleResource);
        assertEquals(TEST_TEXT, string);
        
        hasIOException = false;
        try {
            singleResource.getStream().read();
        } catch (IOException e) {
            hasIOException = true;
        }
        if (!hasIOException) {
            fail("Streem must be closed");
        }

    }
}
