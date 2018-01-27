/**
 * 
 */
package com.github.ginvavilon.traghentto.file;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.StreamUtils;
import com.github.ginvavilon.traghentto.UriConstants;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;

/**
 * @author vbaraznovsky
 *
 */
public class TestFileSource {


    private static final String TEST_TEXT = "Test\n123 456\n";
    private static final String TEST_CHILD_FOLDER = "folder1";
    private static final int TEST_FILE_LENGHT = 13;
    private static final String TEST_CHILD = "child.txt";
    private static final String TEST_FILE = "test.txt";

    private static final String TEST_DIRECTORY = "test";
    private static final String[] TEST_CHILD_FILES = { "file1", "file2", "file3" };

    @Rule
    public TemporaryFolder mTempTestFolder = new TemporaryFolder();

    private FileSource mSingleFileSource;
    private String mAbsoluteSinglePath;
    private FileSource mDirectorySource;
    private File mDirectory;


    @Before
    public void setUp() throws Exception {
        File file = getResourceFile(TEST_FILE);
        mDirectory = getResourceFile(TEST_DIRECTORY);
        mAbsoluteSinglePath = file.getAbsolutePath();
        mSingleFileSource = new FileSource(file);
        mDirectorySource = getResourceFileSource(TEST_DIRECTORY);
    }

    public File getResourceFile(String name) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(name).getFile());
        return file;
    }

    public FileSource getResourceFileSource(String name) {
        return new FileSource(getResourceFile(name));
    }

    @After
    public void tearDown() throws Exception {

    }


    /**
     * Test method for
     * {@link com.github.ginvavilon.traghentto.file.FileSource#getChildren()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetChildren() throws IOException {
        assertNull(mSingleFileSource.getChildren());
        List<? extends FileSource> children = mDirectorySource.getChildren();
        assertNotNull(children);
        Object[] names = children.stream().map(Source::getName).sorted().toArray();
        assertArrayEquals(TEST_CHILD_FILES, names);
    }

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#getChild(java.lang.String)}.
     */
    @Test
    public void testGetChild() {
        for (String childName : TEST_CHILD_FILES) {
            FileSource child = mDirectorySource.getChild(childName);
            File file = new File(mDirectory, childName);
            assertEquals(file, child.getFile());
        }
    }

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#isConteiner()}.
     */
    @Test
    public void testIsConteiner() {
        assertFalse(mSingleFileSource.isConteiner());
    }

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#getPath()}.
     */
    @Test
    public void testGetPath() {
        assertEquals(mAbsoluteSinglePath, mSingleFileSource.getPath());
    }

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#getName()}.
     */
    @Test
    public void testGetName() {
        assertEquals(TEST_FILE, mSingleFileSource.getName());
    }

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#exists()}.
     */
    @Test
    public void testExists() {
        assertTrue(mSingleFileSource.exists());
    }

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#getUriString()}.
     */
    @Test
    public void testGetUriString() {
        assertEquals(UriConstants.FILE_SCHEME + ":" + mAbsoluteSinglePath,
                mSingleFileSource.getUriString());
    }

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#getLenght()}.
     */
    @Test
    public void testGetLenght() {
        assertEquals(TEST_FILE_LENGHT, mSingleFileSource.getLenght());
    }

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#isLocal()}.
     */
    @Test
    public void testIsLocal() {
        assertTrue(mSingleFileSource.isLocal());
    }

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#isDataAvailable()}.
     */
    @Test
    public void testIsDataAvailable() {
        assertTrue(mSingleFileSource.isDataAvailable());
    }

    /**
     * Test method for
     * {@link com.github.ginvavilon.traghentto.file.FileSource#createConteiner()}.
     * 
     * @throws IOException
     */
    @Test
    public void testCreateConteiner() throws IOException {
        File root = mTempTestFolder.newFolder();
        FileSource rootSource = new FileSource(root);
        rootSource.getChild(TEST_CHILD_FOLDER).createConteiner();

        File file = new File(root, TEST_CHILD_FOLDER);
        assertTrue("Conteiner does not exits", file.exists());
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
        File file = new File(root, TEST_CHILD);
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
        assertTrue(file.exists());
        FileSource fileSource = new FileSource(file);
        fileSource.delete();
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
            fail("Streem must be closed");
        }

        StreamResource<InputStream> singleResource = fileSource.openResource(null);
        String string = StreamUtils.readStream(singleResource);
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

    /**
     * Test method for
     * {@link com.github.ginvavilon.traghentto.BaseSource#openResource(com.github.ginvavilon.traghentto.params.StreamParams)}.
     * 
     * @throws IOException
     * @throws IOSourceException
     */
    @Test
    public void testOpenResource() throws IOException, IOSourceException {
        StreamResource<InputStream> singleResource = mSingleFileSource.openResource(null);
        String string = StreamUtils.readStream(singleResource);
        assertEquals(TEST_TEXT, string);
        boolean hasIOException = false;
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
