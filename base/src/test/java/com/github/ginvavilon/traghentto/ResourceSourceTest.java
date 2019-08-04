/**
 * 
 */
package com.github.ginvavilon.traghentto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.github.ginvavilon.traghentto.file.BaseSourceTest;

/**
 * @author vbaraznovsky
 *
 */
public class ResourceSourceTest extends BaseSourceTest<ResourceSource, ResourceSource> {

    private static final String RES_TEST_ASSETS = "res_test_assets";

    @Override
    protected ResourceSource getRootSource() {
        return ResourceSource.CREATOR.create(RES_TEST_ASSETS);
    }

    @Override
    protected void assertDataAvailable(boolean dataAvailable) {
        assertTrue(dataAvailable);
    }

    @Override
    protected void assertLocal(boolean local) {
        assertTrue(local);

    }

    @Override
    public void assertChild(ResourceSource child, String childName) {
        assertEquals(
                ClassLoader.getSystemResource(
                        RES_TEST_ASSETS + "/" + TEST_DIRECTORY + "/" + childName),
                child.getResourceUrl());
    }

    @Override
    public void testIsConteiner() {
        assertFalse(getTestFile().isConteiner());
        assertFalse(getTestDirectory().isConteiner());
    }

    @Override
    protected long getTestFileLenght() {
        return Source.UNKNOWN_LENGHT;
    }

    @Override
    protected void assertStructure(Source root) throws Exception {
        SourceIterator iterator = root.iterator();
        assertNotNull(iterator);
        assertFalse(iterator.hasNext());
    }

    @Override
    public void testGetChildren() throws IOException {
        List<ResourceSource> directoryChildren = getTestDirectory().getChildren();
        assertNotNull(directoryChildren);
        assertTrue(directoryChildren.isEmpty());
        List<ResourceSource> fileChildren = getTestFile().getChildren();
        assertNotNull(fileChildren);
        assertTrue(fileChildren.isEmpty());
    }

}
