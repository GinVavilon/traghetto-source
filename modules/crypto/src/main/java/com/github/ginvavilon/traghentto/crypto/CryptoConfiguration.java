package com.github.ginvavilon.traghentto.crypto;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.crypto.iv.ConstIvGenerator;
import com.github.ginvavilon.traghentto.crypto.iv.DisabledIvGenerator;
import com.github.ginvavilon.traghentto.crypto.iv.HashIvGenerator;
import com.github.ginvavilon.traghentto.crypto.iv.IvGenerator;
import com.github.ginvavilon.traghentto.crypto.salt.NoSalt;
import com.github.ginvavilon.traghentto.crypto.salt.Salt;
import com.github.ginvavilon.traghentto.crypto.salt.SaltFactory;

import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoConfiguration extends KeyCipherProvider {

    private final Map<Integer, Key> mKeys;

    private final KeyProvider mKeyProvider;

    private final Salt mSalt;

    private final String mAlgorithm;

    private final IvGenerator mIvGenerator;

    private CryptoConfiguration(String algorithm, Map<Integer, Key> keys, KeyProvider keyProvider,
            Salt salt,
            IvGenerator ivGenerator) {
        super();
        mAlgorithm = algorithm;
        mKeys = keys;
        mKeyProvider = keyProvider;
        mSalt = salt;
        mIvGenerator = ivGenerator;
    }

    public String getAlgorithm() {
        return mAlgorithm;
    }

    public Salt getSalt() {
        return mSalt;
    }

    public Key getPrivateKey() {
        return getKey(Cipher.DECRYPT_MODE);
    }

    public Key getPublicKey() {
        return getKey(Cipher.ENCRYPT_MODE);
    }

    public Key getKey(int mode) {
        Key key = mKeys.get(mode);
        if ((key == null) && (mKeyProvider != null)) {
            key = mKeyProvider.getKey(mode);
            mKeys.put(mode, key);
        }
        return key;
    }

    public byte[] getIv(int blockSize) {
        return mIvGenerator.generateIv(blockSize);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        Builder builder = new Builder();
        builder.setConfiguration(this);
        builder.mKeys.putAll(mKeys);
        return builder;
    }

    public static class Builder {
        private static final String ALGORITHM_FORMAT = "%s/%s/%s";

        private Map<Integer, Key> mKeys = new HashMap<Integer, Key>();
        private Salt mSalt = new NoSalt();
        private String mAlgorithm = Crypto.DEFAULT;
        private KeyProvider mKeyProvider;
        private IvGenerator mIvGenerator;
        private String mMode;
        private String mPadding;

        public Builder setAlgorithm(String algorithm) {
            mAlgorithm = algorithm;
            return this;
        }

        public Builder setConfiguration(CryptoConfiguration configuration) {
            String[] parts = configuration.mAlgorithm.split("/");
            mAlgorithm = parts[0];
            mMode = parts[1];
            mPadding = parts[2];
            mIvGenerator = configuration.mIvGenerator;
            mSalt = configuration.mSalt;
            return this;
        }

        public Builder setKey(Key key) {
            putKey(key);
            mAlgorithm = key.getAlgorithm();
            return this;
        }

        public Builder setPrivateKey(Key key) {
            mKeys.put(Cipher.DECRYPT_MODE, key);
            SecurityLogger.println("private-key", key.getEncoded());
            return this;
        }

        public Builder setKeyProvider(KeyProvider keyProvider) {
            mKeyProvider = keyProvider;
            return this;
        }

        public Builder setPublicKey(Key key) {
            mKeys.put(Cipher.ENCRYPT_MODE, key);
            SecurityLogger.println("public-key", key.getEncoded());
            return this;
        }

        private void putKey(Key key) {
            mKeys.put(Cipher.DECRYPT_MODE, key);
            mKeys.put(Cipher.ENCRYPT_MODE, key);
        }

        public Builder setMode(String mode) {
            mMode = mode;
            return this;
        }

        public Builder setPadding(String padding) {
            mPadding = padding;
            return this;
        }

        public Builder setKey(byte[] key) {
            Key secretKeySpec = new SecretKeySpec(key, mAlgorithm);
            putKey(secretKeySpec);
            return this;
        }

        public Builder setKey(String key) {
            return setKey(key.getBytes());
        }

        public Builder usePassword(String password)
                throws InvalidKeySpecException, NoSuchAlgorithmException {
            return usePassword(password, Crypto.DEFAULT_HASH);
        }

        public Builder setPBEKey(String password)
                throws NoSuchAlgorithmException, InvalidKeySpecException {
            return setPBEKey(password, Crypto.DEFAULT_HASH);
        }

        public Builder setPBEKey(String password, String pbeAlgorithm)
                throws NoSuchAlgorithmException, InvalidKeySpecException {
            String generationFactory = CryptoUtils.getPBEKeyGenerationFactory(pbeAlgorithm,
                    mAlgorithm);
            SecurityLogger.println("pbe-factory", generationFactory);
            int length = CryptoUtils.getKeySizeBits(mAlgorithm, 256);
            final SecretKeyFactory factory = SecretKeyFactory.getInstance(generationFactory);
            final KeySpec cipherSpec = new PBEKeySpec(password.toCharArray());
            SecretKey secretKey = factory.generateSecret(cipherSpec);
            byte[] encoded = secretKey.getEncoded();
            SecurityLogger.println("pbe-key", encoded);
            putKey(secretKey);
            return this;
        }

        public Builder setPBKDF2Key(String password, byte[] salt, int iteration)
                throws NoSuchAlgorithmException, InvalidKeySpecException {
            return setPBKDF2Key(password, salt, iteration, Crypto.DEFAULT_HASH);
        }

        public Builder setPBKDF2Key(String password, byte[] salt, int iteration,
                String hashAlgorithm)
                throws NoSuchAlgorithmException, InvalidKeySpecException {
            String generationFactory = CryptoUtils.getPBKDF2KeyGenerationFactory(hashAlgorithm);
            int length = CryptoUtils.getKeySizeBits(mAlgorithm,
                    CryptoUtils.detectHashSize(hashAlgorithm));
            SecurityLogger.println("bits", length);
            SecurityLogger.println("factory", generationFactory);
            final SecretKeyFactory factory = SecretKeyFactory.getInstance(generationFactory);

            final KeySpec cipherSpec = new PBEKeySpec(password.toCharArray(), salt, iteration,
                    length);
            SecretKey secretKey = factory.generateSecret(cipherSpec);
            byte[] encoded = secretKey.getEncoded();
            SecurityLogger.println("pbkf2-key", encoded);

            putKey(secretKey);
            return this;
        }

        public Builder usePassword(String password, String hashAlgorithm)
                throws NoSuchAlgorithmException {
            SecurityLogger.println("hash", hashAlgorithm);
            MessageDigest instance = MessageDigest.getInstance(hashAlgorithm);
            byte[] hash = instance.digest(password.getBytes());

            int length = CryptoUtils.getKeySizeBytes(mAlgorithm, hash.length);
            SecurityLogger.println("length", length * 8);
            int oldLength = hash.length;
            if (oldLength != length) {
                hash = Arrays.copyOf(hash, length);
            }

            while (oldLength < length) {
                instance.update(hash, 0, oldLength);
                byte[] digest = instance.digest();
                System.arraycopy(digest, 0, hash, oldLength,
                        Math.min(length - oldLength, digest.length));
                oldLength = oldLength + digest.length;
            }

            if (mIvGenerator == null && (CryptoUtils.isSupportIv(mAlgorithm, mMode))) {
                mIvGenerator = HashIvGenerator.create(hashAlgorithm, hash);
                SecurityLogger.println("iv", mIvGenerator.generateIv(length));
            }

            SecurityLogger.println("hash-key", hash);
            putKey(new SecretKeySpec(hash, mAlgorithm));
            return this;

        }

        public Builder loadPublicKey(Source source)
                throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
            Key publicKey = CryptoUtils.loadPublicKey(mAlgorithm, source);
            return setPublicKey(publicKey);
        }

        public Builder loadPrivateKey(Source source)
                throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
            Key privateKey = CryptoUtils.loadPrivateKey(mAlgorithm, source);
            return setPrivateKey(privateKey);
        }

        public Builder generatePairKey(int keySize) throws NoSuchAlgorithmException {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(mAlgorithm);
            generator.initialize(keySize);
            KeyPair keyPair = generator.generateKeyPair();
            return setKeyPair(keyPair);
        }

        public Builder generateKey(int keysize) throws NoSuchAlgorithmException {

            KeyGenerator generator = KeyGenerator.getInstance(mAlgorithm);
            generator.init(keysize);
            SecretKey key = generator.generateKey();
            putKey(key);
            return this;
        }

        private Builder setKeyPair(KeyPair keyPair) {
            setPrivateKey(keyPair.getPrivate());
            setPublicKey(keyPair.getPublic());
            return this;
        }

        public CryptoConfiguration build() {

            Map<Integer, Key> keys = this.mKeys;
            Salt salt = mSalt;
            String mode = mMode;
            if (mode == null) {
                mode = CryptoUtils.getDefaultMode(mAlgorithm);
            }
            String padding = mPadding;
            if (padding == null) {
                padding = CryptoUtils.getDefaultPadding(mAlgorithm);
            }

            IvGenerator ivGenerator = mIvGenerator;
            if (ivGenerator == null) {
                ivGenerator = CryptoUtils.createEmptyIvGenerator(mode);
            }
            String algorithm = String.format(ALGORITHM_FORMAT, mAlgorithm, mode, padding);
            return new CryptoConfiguration(algorithm, keys, mKeyProvider, salt, ivGenerator);
        }

        public Builder disableIv() {
            mIvGenerator = new DisabledIvGenerator();
            return this;
        }

        public Builder addRandomSalt(int size) {
            mSalt = SaltFactory.createRandom(size);
            return addSalt(SaltFactory.createRandom(size));
        }

        public Builder addRandomSalt(int min, int max) {
            return addSalt(SaltFactory.createRandom(min, max));
        }

        public Builder disableSalt(int min, int max) {
            return addSalt(Crypto.NO_SALT);
        }

        public Builder addSalt(Salt salt) {
            mSalt = salt;
            return this;
        }

        public Builder addFixedSalt(int size) {
            return addSalt(SaltFactory.createRandomWithFixedSize(size));
        }

        public Builder addSalt(byte[] salt) {
            return addSalt(SaltFactory.createSalt(salt));
        }

        public Builder addIv(IvGenerator ivGenerator) {
            mIvGenerator = ivGenerator;
            return this;
        }

        public Builder addIv(byte[] iv) {
            mIvGenerator = new ConstIvGenerator(iv);
            return this;
        }

    }

}