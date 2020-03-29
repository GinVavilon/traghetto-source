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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceIterator;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.StreamUtils;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;

/**
 * @author Vladimir Baraznovsky
 *
 */
public abstract class BaseSourceTest<TRoot extends Source, TChild extends Source> {

    private static final String SEPARATOR = "/";
    protected static final String TEST_TEXT = "Test\n123 456\n";
    private TChild mTestFileSource;
    private TChild mTestDirectorySource;

    protected static final String TEST_CHILD_FOLDER = "folder1";
    private static final int TEST_FILE_LENGTH = 13;
    protected static final String TEST_CHILD = "child.txt";
    protected static final String TEST_FILE = "test.txt";
    protected static final String TEST_DIRECTORY = "test";
    protected static final String[] TEST_CHILD_FILES = { "file1", "file2", "file3" };
    protected static final Set<String> ALL_FILES = new HashSet<>();
    static {
        ALL_FILES.add(TEST_FILE);
        ALL_FILES.add(TEST_DIRECTORY);
        for (String child : TEST_CHILD_FILES) {
            ALL_FILES.add(TEST_DIRECTORY + SEPARATOR + child);
        }
    }

    @Rule
    public TemporaryFolder mTempTestFolder = new TemporaryFolder();

    protected TRoot mRoot;

    public BaseSourceTest() {
        super();
    }


    @Before
    public void setUpSources() throws Exception {

        mRoot = getRootSource();
        mTestFileSource = (TChild) mRoot.getChild(TEST_FILE);
        if (mTestFileSource == null){
            throw new FileNotFoundException("File " + TEST_FILE + " is not found in " + mRoot);
        }
        mTestDirectorySource = (TChild) mRoot.getChild(TEST_DIRECTORY);

        if (mTestDirectorySource == null){
            throw new FileNotFoundException("Directory " + TEST_DIRECTORY + " is not found in " + mRoot);
        }

    }

    public TChild getTestFile() {
        return mTestFileSource;
    }

    public TChild getTestDirectory() {
        return mTestDirectorySource;
    }

    @Test
    public void testIterator() throws Exception {
        assertStructure(mRoot);
    }

    protected void assertStructure(Source root) throws Exception {
        Set<String> files = new HashSet<>(ALL_FILES);
        String rootPath = root.getPath();
        if (!rootPath.endsWith(SEPARATOR)) {
            rootPath += SEPARATOR;
        }

        SourceIterator iterator = root.iterator();
        Source next;
        while (iterator.hasNext()) {
            next = iterator.next();
            String path = getRelativePath(rootPath, next.getPath());
            assertTrue("File not expected " + path, files.contains(path));
            files.remove(path);
        }
        assertTrue(files.isEmpty());
        iterator.close();
    }


    protected abstract TRoot getRootSource();

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#isContainer()}.
     */
    @Test
    public void testIsContainer() {
        assertFalse(getTestFile().isContainer());
        assertTrue(getTestDirectory().isContainer());
    }

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#getName()}.
     */
    @Test
    public void testGetName() {
        assertEquals(TEST_FILE, getTestFile().getName());
        assertEquals(TEST_DIRECTORY, getTestDirectory().getName());
    }

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#exists()}.
     */
    @Test
    public void testExists() {
        assertTrue(getTestFile().exists());
        assertTrue(getTestDirectory().exists());
        assertFalse(getTestDirectory().getChild(TEST_CHILD).exists());
    }

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#getLength()}.
     */
    @Test
    public void testGetLength() {
        assertEquals(getTestFileLength(), getTestFile().getLength());
    }


    protected long getTestFileLength() {
        return TEST_FILE_LENGTH;
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
        StreamResource<InputStream> singleResource = getTestFile().openResource(null);
        String string = StreamUtils.readStringFromResource(singleResource);
        assertEquals(TEST_TEXT, string);
        assertTrue("Stream must be closed", checkClosedStream(singleResource));
    }


    protected boolean checkClosedStream(StreamResource<InputStream> singleResource) {
        boolean streamClosed = false;
        try {
            int res = singleResource.getStream().read();
        } catch (IOException e) {
            streamClosed = true;
        }
        return streamClosed;
    }

    protected abstract void assertDataAvailable(boolean dataAvailable);

    protected abstract void assertLocal(boolean local);

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#isLocal()}.
     */
    @Test
    public void testIsLocal() {
        assertLocal(getTestFile().isLocal());
    }

    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#isDataAvailable()}.
     */
    @Test
    public void testIsDataAvailable() {
        assertDataAvailable(getTestFile().isDataAvailable());
    }

    public File getResourceFile(String name) {
        return getResourceFile(this, name);
    }

    public static File getResourceFile(Object test, String name) {
        ClassLoader classLoader = test.getClass().getClassLoader();
        URL resource = classLoader.getResource(name);
        if (resource == null) {
            throw new IllegalArgumentException(name + " do not exist");
        }
        String fileName = resource.getFile();
        if (fileName == null) {
            throw new IllegalArgumentException(name + " is not file");
        }

        File file = new File(fileName);
        return file;
    }


    public abstract void assertChild(TChild child, String childName);


    /**
     * Test method for {@link com.github.ginvavilon.traghentto.file.FileSource#getChild(java.lang.String)}.
     */
    @Test
    public void testGetChild() {
        for (String childName : TEST_CHILD_FILES) {
            TChild child = (TChild) getTestDirectory().getChild(childName);
            assertChild(child, childName);
            assertTrue(child.exists());
        }

        TChild child = (TChild) getTestDirectory().getChild(TEST_FILE);
        assertChild(child, TEST_FILE);
        assertFalse(child.exists());
    }


    /**
     * Test method for
     * {@link com.github.ginvavilon.traghentto.file.FileSource#getChildren()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetChildren() throws IOException {
        assertNull(getTestFile().getChildren());
        List<? extends Source> children = getTestDirectory().getChildren();
        assertNotNull(children);
        Object[] names = children.stream().map(Source::getName).sorted().toArray();
        assertArrayEquals(TEST_CHILD_FILES, names);
    }

    protected String getRelativePath(String rootPath, String path) {


        if (path.startsWith(rootPath)) {
            path = path.substring(rootPath.length());
        }
        while (path.endsWith(SEPARATOR)) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

}