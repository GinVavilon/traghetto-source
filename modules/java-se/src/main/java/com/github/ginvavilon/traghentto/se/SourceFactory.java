/**
 *
 */
package com.github.ginvavilon.traghentto.se;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.github.ginvavilon.traghentto.ISource;
import com.github.ginvavilon.traghentto.SourceCreator;
import com.github.ginvavilon.traghentto.UriConstants;
import com.github.ginvavilon.traghentto.file.CachedSource;
import com.github.ginvavilon.traghentto.file.DiskLruCache;
import com.github.ginvavilon.traghentto.file.FileSource;
import com.github.ginvavilon.traghentto.http.apache.ApacheHttpSource;
import com.github.ginvavilon.traghentto.zip.ZipRandomAccessFileSource;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class SourceFactory implements UriConstants {

    private static final Map<String, Data> sCreators = new HashMap<>();
    private static Data sDefault;

    static {

        register(HTTPS_SCHEME, ApacheHttpSource.CREATOR);
        register(HTTP_SCHEME, ApacheHttpSource.CREATOR);
        registerPath(ZIP_FILE_SCHEME, ZipRandomAccessFileSource.CREATOR);
        registerPath(FILE_SCHEME, FileSource.CREATOR);
        setDefault(FileSource.CREATOR);

    }

    public static void register(String protocol, SourceCreator<?> creator) {
        sCreators.put(protocol, new Data(false, creator));
    }

    public static void registerPath(String protocol, SourceCreator<?> creator) {
        sCreators.put(protocol, new Data(true, creator));
    }

    public static void setDefault(SourceCreator<?> creator) {
        sDefault = new Data(false, creator);
    }

    public static void setDefaultPath(SourceCreator<?> creator) {
        sDefault = new Data(true, creator);
    }

    private static ISource createBasePathUri(URI uri) {
        Data data = sCreators.get(uri.getScheme());
        if (data == null) {
            data = sDefault;
        }
        String param = data.isPath() ? uri.getPath() : uri.toString();
        return data.getCreator().create(param);

    }

    public static ISource createFromUri(URI url) {
        ISource source = createBasePathUri(url);
        if (url.getFragment() != null) {
            source = source.getChild(url.getFragment());
        }
        return source;
    }

    public static ISource createCachedIfNeed(ISource pSource, DiskLruCache pDiskLruCache) {
        if (pSource.isLocal()) {
            return pSource;
        } else {
            return new CachedSource<ISource>(pDiskLruCache, pSource);
        }
    }

    public static ISource createFromUrl(String pUri) throws MalformedURLException {
        return createFromUri(URI.create(pUri));

    }

    public static ISource createChild(ISource pParent, String pUri) throws MalformedURLException {
        URI uri = URI.create(pUri);
        ISource source;
        if (!uri.isAbsolute()) {
            source = pParent.getChild(pUri);
        } else {
            source = createFromUrl(pUri);
        }
        return source;
    };

    private static class Data {
        private final boolean path;
        private final SourceCreator<?> creator;

        public Data(boolean pPath, SourceCreator<?> pCreator) {
            super();

            path = pPath;
            creator = pCreator;
        }

        public boolean isPath() {
            return path;
        }

        public SourceCreator<?> getCreator() {
            return creator;
        }

    }

}
