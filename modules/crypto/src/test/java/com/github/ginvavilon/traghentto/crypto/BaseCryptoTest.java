package com.github.ginvavilon.traghentto.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.junit.Before;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.file.BaseSourceTest;
import com.github.ginvavilon.traghentto.file.FileSource;

public abstract class BaseCryptoTest extends BaseSourceTest<CryptoSource<?>, CryptoSource<?>> {

    private File mDirectory;
    private CryptoConfiguration mConfiguration;

    @Before
    public void initConfiguration() {
        getConfiguration();
    }

    protected CryptoConfiguration getConfiguration() {
        if (mConfiguration == null) {
            try {
                mConfiguration = createConfiguration();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return mConfiguration;
    }

    protected CryptoConfiguration createConfiguration() throws NoSuchAlgorithmException {
        return CryptoConfiguration.builder()
                .setAlgorithm(getAlgorithm())
                .setMode(getMode())
                .usePassword("test", getHash())
                .build();
    }

    protected abstract String getHash();

    protected abstract String getMode();

    protected abstract String getAlgorithm();

    protected abstract String getAssetsDirectory();

    public BaseCryptoTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        File root = getResourceFile("");
        mDirectory = new File(root, TEST_DIRECTORY);
    
    }

    @Override
    public File getResourceFile(String name) {
        return super.getResourceFile(getAssetsDirectory() + name);
    }

    public FileSource getResourceFileSource(String name) {
        return new FileSource(getResourceFile(name));
    }

    @Override
    protected CryptoSource<?> getRootSource() {
        FileSource source = getResourceFileSource("");
        return new CryptoSource<>(source, getConfiguration());
    }

    @Override
    protected boolean checkClosedStream(StreamResource<InputStream> singleResource) {
        try {
            return (super.checkClosedStream(singleResource)) || (singleResource.getStream().read() == -1);
        } catch (IOException e) {
            return true;
        }
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
    public void assertChild(CryptoSource<?> child, String childName) {
        File file = new File(mDirectory, childName);
        assertEquals(file.getAbsolutePath(), child.getPath());
    }

    @Override
    protected long getTestFileLenght() {
        return Source.UNKNOWN_LENGHT;
    }

}