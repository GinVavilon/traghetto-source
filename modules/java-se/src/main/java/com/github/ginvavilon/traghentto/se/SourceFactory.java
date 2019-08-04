/**
 *
 */
package com.github.ginvavilon.traghentto.se;

import java.net.MalformedURLException;
import java.net.URI;

import com.github.ginvavilon.traghentto.BaseSourceFactory;
import com.github.ginvavilon.traghentto.ResourceSource;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.SourceCreator;
import com.github.ginvavilon.traghentto.UriConstants;
import com.github.ginvavilon.traghentto.file.CachedSource;
import com.github.ginvavilon.traghentto.file.DiskLruCache;
import com.github.ginvavilon.traghentto.file.FileSource;
import com.github.ginvavilon.traghentto.http.apache.ApacheHttpSource;
import com.github.ginvavilon.traghentto.path.PathSource;
import com.github.ginvavilon.traghentto.zip.ZipRandomAccessFileSource;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class SourceFactory implements UriConstants {

    private static final String RES_SCHEME = "res";

    private static final BaseSourceFactory<URI> sFactory = new BaseSourceFactory.Builder<URI>()
            .parser(URI::create)
            .path(URI::getPath)
            .protocol(URI::getScheme)
            .checkAbsolute(URI::isAbsolute)
            .childGetter(URI::getFragment)
            .uri(URI::toString)
            .build();

    static {

        try {
            Class.forName("com.github.ginvavilon.traghentto.http.apache.ApacheHttpSource");
            register(HTTPS_SCHEME, ApacheHttpSource.CREATOR);
            register(HTTP_SCHEME, ApacheHttpSource.CREATOR);
        } catch (ClassNotFoundException e) {
        }
        registerPath(ZIP_FILE_SCHEME, ZipRandomAccessFileSource.CREATOR);
        registerPath(RES_SCHEME, ResourceSource.CREATOR);
        registerPath(RESOURCE_SCHEME, ResourceSource.CREATOR);
        usePathSource(true);
        setDefault(FileSource.CREATOR);
    }

    public static void register(String protocol, SourceCreator<?> creator) {
        sFactory.register(protocol, creator);
    }

    public static void registerPath(String protocol, SourceCreator<?> creator) {
        sFactory.registerPath(protocol, creator);
    }

    public static void setDefault(SourceCreator<?> creator) {
        sFactory.setDefault(creator);
    }

    public static void setDefaultPath(SourceCreator<?> creator) {
        sFactory.setDefaultPath(creator);
    }

    public static Source create(String uri) {
        return sFactory.createFromUri(uri);
    }

    public static void usePathSource(boolean use) {
        if (use) {
            registerPath(FILE_SCHEME, PathSource.CREATOR);
        } else {
            registerPath(FILE_SCHEME, FileSource.CREATOR);
        }
    }


    public static Source createFromUri(URI uri) {
        return sFactory.create(uri);
    }

    public static Source createCachedIfNeed(Source pSource, DiskLruCache pDiskLruCache) {
        if (pSource.isLocal()) {
            return pSource;
        } else {
            return new CachedSource<Source>(pDiskLruCache, pSource);
        }
    }

    public static Source createFromUrl(String pUri) throws MalformedURLException {
        return sFactory.createFromUri(pUri);
    }

    public static Source createChild(Source pParent, String pUri) throws MalformedURLException {
        URI uri = URI.create(pUri);
        Source source;
        if (!uri.isAbsolute()) {
            source = pParent.getChild(pUri);
        } else {
            source = createFromUrl(pUri);
        }
        return source;
    };



}
