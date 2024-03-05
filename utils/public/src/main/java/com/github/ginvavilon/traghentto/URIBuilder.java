/**
 *
 */
package com.github.ginvavilon.traghentto;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class URIBuilder {

    private String mScheme;
    private String mAuthority;
    private String mFragment;
    private String mPath;

    public URIBuilder() {
    }

    public void scheme(String pScheme) {
        mScheme = pScheme;

    }

    public void authority(String pAuthority) {
        mAuthority = pAuthority;

    }

    public void fragment(String pFragment) {
        mFragment = pFragment;

    }

    public URI build() {

        try {
            return new URI(mScheme, mAuthority, mPath, null, mFragment);
        } catch (URISyntaxException e) {
            Logger.e(e);
            return null;
        }
    }

    public void encodedFragment(String pFragment) {

        try {
            fragment(URLEncoder.encode(pFragment, Charset.defaultCharset().name()));
        } catch (UnsupportedEncodingException e) {

        }

    }

    public void path(String pPath) {
        mPath = pPath;

    }

}
