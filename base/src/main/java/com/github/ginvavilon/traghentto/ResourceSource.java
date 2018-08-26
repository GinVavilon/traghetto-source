/**
 * 
 */
package com.github.ginvavilon.traghentto;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author vbaraznovsky
 *
 */
public class ResourceSource extends BaseSource {

    private static final char SEPARATOR = '/';

    private final URL mResource;
    private final ClassLoader mClassLoader;
    private final String mFullName;

    public ResourceSource(ClassLoader classLoader, String fullName) {
        mClassLoader = classLoader;
        mFullName = fullName;
        mResource = classLoader.getResource(fullName);
    }

    public URL getResourceUrl() {
        return mResource;
    }
    @Override
    public List<ResourceSource> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public ResourceSource getChild(String name) {
        return new ResourceSource(mClassLoader, mFullName + SEPARATOR + name);
    }

    @Override
    public boolean isConteiner() {
        return false;
    }

    @Override
    public String getPath() {
        return mFullName;
    }

    @Override
    public String getName() {
        String path = getPath();
        return path.substring(path.lastIndexOf(SEPARATOR) + 1);
    }

    @Override
    public String getUriString() {
        return UriConstants.RESOURCE_SCHEME + ":/" + mFullName;
    }

    @Override
    public boolean exists() {
        return mResource != null;
    }

    @Override
    public long getLenght() {
        return UNKNOWN_LENGHT;
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public boolean isDataAvailable() {
        return exists();
    }

    @Override
    protected InputStream openInputStream(StreamParams pParams)
            throws IOException, IOSourceException {
        if (!exists()) {
            throw new IOSourceException(String.format("Resource %s is not exist", mFullName));
        }
        return mResource.openStream();
    }

    public static final SourceCreator<ResourceSource> CREATOR = new SourceCreator<ResourceSource>() {

        @Override
        public ResourceSource create(String param) {

            if (param.charAt(0) == SEPARATOR) {
                param = param.substring(1);
            }

            return new ResourceSource(getClass().getClassLoader(), param);
        }
    };
}
