package com.github.ginvavilon.traghentto.crypto;

import java.util.Arrays;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.github.ginvavilon.traghentto.crypto.Crypto.Algorithm;
import com.github.ginvavilon.traghentto.crypto.Crypto.Hash;
import com.github.ginvavilon.traghentto.crypto.Crypto.Mode;

@RunWith(Parameterized.class)
public class DecryptionTest extends BaseCryptoTest {

    private String mMode;
    private String mHash;
    private String mAlgorithm;
    private String mAssetsDirectory;

    @Parameters(name = "{index}: {1}/{2} ({3})")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "aes-128-cbc-sha1", Algorithm.AES, Mode.CBC, Hash.SHA1 },

                { "aes-128-ecb-md5", Algorithm.AES, Mode.ECB, Hash.MD5 },
                { "aes-128-ecb-sha1", Algorithm.AES, Mode.ECB, Hash.SHA1 },
                { "aes-256-ecb-sha256", Algorithm.AES, Mode.ECB, Hash.SHA256 },
                { "aes-256-ecb-sha512", Algorithm.AES, Mode.ECB, Hash.SHA512 },

                { "des-ecb-md5", Algorithm.DES, Mode.ECB, Hash.MD5 },
                { "des-ecb-sha1", Algorithm.DES, Mode.ECB, Hash.SHA1 },
                { "des-ecb-sha256", Algorithm.DES, Mode.ECB, Hash.SHA256 },
                { "des-ecb-sha512", Algorithm.DES, Mode.ECB, Hash.SHA512 },

                { "rc2-ecb-md5", Algorithm.RC2, Mode.ECB, Hash.MD5 },
                { "rc2-ecb-sha1", Algorithm.RC2, Mode.ECB, Hash.SHA1 },
                { "rc2-ecb-sha256", Algorithm.RC2, Mode.ECB, Hash.SHA256 },
                { "rc2-ecb-sha512", Algorithm.RC2, Mode.ECB, Hash.SHA512 },


        });
    }

    public DecryptionTest(String assetsDirectory, String algorithm, String mode, String hash) {
        super();
        mAssetsDirectory = "encrypted-assets/" + assetsDirectory + "/";
        mAlgorithm = algorithm;
        mMode = mode;
        mHash = hash;
    }


    @Override
    protected String getHash() {
        return mHash;
    }

    @Override
    protected String getMode() {
        return mMode;
    }

    @Override
    protected String getAlgorithm() {
        return mAlgorithm;
    }

    @Override
    protected String getAssetsDirectory() {
        return mAssetsDirectory;
    }

}
