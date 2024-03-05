package com.github.ginvavilon.traghentto.crypto;

import static org.junit.Assert.assertEquals;

import com.github.ginvavilon.traghentto.IOSourceUtils;
import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.Logger.LogHandler;
import com.github.ginvavilon.traghentto.SourceUtils;
import com.github.ginvavilon.traghentto.crypto.Crypto.Algorithm;
import com.github.ginvavilon.traghentto.crypto.Crypto.KeySize;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.exceptions.SourceAlreadyExistsException;
import com.github.ginvavilon.traghentto.file.FileSource;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class EncryptionTest {
    private static final String[] FILES = new String[] { "test.txt", "test/file1", "test/file2",
            "test/file3" };
    private static final String ASSETS = "test-assets";

    @BeforeClass
    public static void initLogger() {
        Logger.register(new LogHandler() {

            @Override
            public void i(int pType, String pMessage, Object[] pArgs) {
                System.out.printf(pMessage, pArgs);
                System.out.println();
            }

            @Override
            public void e(int pType, String pMessage, Object[] pArgs, Throwable pThrowable) {
                System.err.printf(pMessage, pArgs);
                System.err.println();
                e(pType, pThrowable);
            }

            @Override
            public void e(int pType, Throwable pE) {
                pE.printStackTrace(System.err);
            }

            @Override
            public void d(int pType, String pMessage, Object[] pArgs) {
                System.out.printf(pMessage, pArgs);
                System.out.println();

            }
        });
    }

    @Rule
    public TemporaryFolder mTempTestFolder = new TemporaryFolder();

    @Test
    public void testRsa()
            throws IOException, NoSuchAlgorithmException, IOSourceException,
            SourceAlreadyExistsException,
            InvalidKeySpecException {
        CryptoConfiguration configuration = CryptoConfiguration.builder()
                .setAlgorithm(Algorithm.RSA)
                .generatePairKey(KeySize.RSA_1024)
                .addRandomSalt(0, 10)
                .build();

        testEncryption(configuration);

    }

    @Test
    public void testAes()
            throws IOException, NoSuchAlgorithmException, IOSourceException,
            SourceAlreadyExistsException,
            InvalidKeySpecException {
        testSymmetricEncryption(Algorithm.AES, KeySize.AES_128);

    }

    protected void testSymmetricEncryption(String algorithm, int keySize)
            throws NoSuchAlgorithmException,
            InvalidKeySpecException, IOSourceException, SourceAlreadyExistsException, IOException {
        CryptoConfiguration configuration = CryptoConfiguration.builder()
                .setAlgorithm(algorithm)
                .generateKey(keySize)
                .addRandomSalt(512, 1024)
                .build();

        testEncryption(configuration);
    }

    @Test
    public void testDes()
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOSourceException,
            SourceAlreadyExistsException, IOException {
        testSymmetricEncryption(Algorithm.DES, KeySize.DES_GENERATED);
    }

    @Test
    public void testDesEge()
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOSourceException,
            SourceAlreadyExistsException, IOException {
        testSymmetricEncryption(Algorithm.DES_EDE, KeySize.DES_EDE_128_GENERATED);
    }

    @Test
    public void testBlowfishMin()
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOSourceException,
            SourceAlreadyExistsException, IOException {
        testSymmetricEncryption(Algorithm.BLOWFISH, KeySize.BLOWFISH_MIN);
    }

    @Test
    public void testBlowfishMax()
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOSourceException,
            SourceAlreadyExistsException, IOException {
        testSymmetricEncryption(Algorithm.BLOWFISH, KeySize.BLOWFISH_MAX);
    }

    protected void testEncryption(CryptoConfiguration configuration)
            throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException, IOSourceException, SourceAlreadyExistsException {
        File temp = mTempTestFolder.newFolder();
        // temp = BaseCryptoTest.getResourceFile(this, "");

        File testRoot = BaseCryptoTest.getResourceFile(this, ASSETS);

        File outRoot = new File(temp, "out");
        File decRoot = new File(temp, "decrypted-out");
        File keyRoot = new File(temp, "keys");

        FileSource outSource = new FileSource(outRoot);
        FileSource inSource = new FileSource(testRoot);
        FileSource decSource = new FileSource(decRoot);
        outSource.createContainer();
        decSource.createContainer();

        FileSource keysSource = new FileSource(keyRoot);
        FileSource privateKeySource = keysSource.getChild("rsa.key");
        FileSource publicKeySource = keysSource.getChild("rsa.pub");

        CryptoUtils.savePrivateKey(configuration.getPrivateKey(), privateKeySource);
        CryptoUtils.savePublicKey(configuration.getPublicKey(), publicKeySource);

        CryptoConfiguration outConfiguration = CryptoConfiguration.builder()
                .setConfiguration(configuration)
                .loadPrivateKey(privateKeySource)
                .loadPublicKey(publicKeySource)
                .build();
        EncryptoSource<?> encryptoSource = Crypto.encode(outSource, configuration);
        SourceUtils.replace(inSource, encryptoSource);

        CryptoSource<?> decryptoSource = Crypto.decode(outSource, outConfiguration);
        SourceUtils.replace(decryptoSource, decSource);
        
        for (String name : FILES) {

            String decryptedText = IOSourceUtils.readStringFromSource(decSource.getChild(name));

            String originalText = IOSourceUtils.readStringFromSource(inSource.getChild(name));
            assertEquals(originalText, decryptedText);
        }
    }

}
