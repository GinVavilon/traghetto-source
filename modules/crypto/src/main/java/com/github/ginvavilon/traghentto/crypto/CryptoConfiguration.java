package com.github.ginvavilon.traghentto.crypto;

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
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.crypto.iv.ConstIvGenerator;
import com.github.ginvavilon.traghentto.crypto.iv.DisabledIvGenerator;
import com.github.ginvavilon.traghentto.crypto.iv.HashIvGenerator;
import com.github.ginvavilon.traghentto.crypto.iv.IvGenerator;
import com.github.ginvavilon.traghentto.crypto.salt.NoSalt;
import com.github.ginvavilon.traghentto.crypto.salt.Salt;
import com.github.ginvavilon.traghentto.crypto.salt.SaltFactory;

public class CryptoConfiguration {

	private final Map<Integer,Key> mKeys;

	private final Salt mSalt;

	private final String mAlgorithm;

	private final IvGenerator mIvGenerator;

	private CryptoConfiguration(String algorithm, Map<Integer, Key> keys, Salt salt, IvGenerator ivGenrator) {
		super();
		mAlgorithm = algorithm;
		mKeys = keys;
		mSalt = salt;
		mIvGenerator = ivGenrator;
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
		return mKeys.get(mode);
	}

	public byte[] getIv(int blockSize) {
		return mIvGenerator.generateIv(blockSize);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private static final String ALGORITHM_FORMAT = "%s/%s/%s";

		private Map<Integer,Key> mKeys = new HashMap<Integer, Key>();
		private Salt mSalt = new NoSalt();
		private String mAlgorithm = Crypto.DEFAULT;
		private IvGenerator mIvGenerator;
        private String mMode;
        private String mPadding;

		public Builder setAlgorithm(String algorithm) {
			mAlgorithm = algorithm;
			return this;
		}

		public Builder setKey(Key key) {
			putKey(key);
			mAlgorithm = key.getAlgorithm();
			return this;
		}
		public Builder setPrivateKey(Key key) {
			mKeys.put(Cipher.DECRYPT_MODE, key);
            CryptoUtils.println("private-key", key.getEncoded());
			return this;
		}
		
		public Builder setPublicKey(Key key) {
			mKeys.put(Cipher.ENCRYPT_MODE, key);
            CryptoUtils.println("public-key", key.getEncoded());
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

		public Builder usePassword(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {

			return usePassword(password, Crypto.DEFAULT_HASH);
		}

		public Builder setPBEKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
			return setPBEKey(password,Crypto.DEFAULT_HASH);
		}
		
		public Builder setPBEKey(String password, String pbeAlgorithm)
				throws NoSuchAlgorithmException, InvalidKeySpecException {
			String generationFactory = CryptoUtils.getPBEKeyGenerationFactory(pbeAlgorithm, mAlgorithm);
			CryptoUtils.println("pbe-factory", generationFactory);
			int lenght = CryptoUtils.getKeySizeBits(mAlgorithm, 256);
			final SecretKeyFactory factory = SecretKeyFactory.getInstance(generationFactory);
			final KeySpec cipherSpec = new PBEKeySpec(password.toCharArray());
			SecretKey secretKey = factory.generateSecret(cipherSpec);
			byte[] encoded = secretKey.getEncoded();
			CryptoUtils.println("pbe-key", encoded);
			putKey(secretKey);
			return this;
		}
		public Builder setPBKDF2Key(String password, byte[] salt, int iteration)
				throws NoSuchAlgorithmException, InvalidKeySpecException {
			return setPBKDF2Key(password, salt, iteration, Crypto.DEFAULT_HASH);
		}
		
		public Builder setPBKDF2Key(String password, byte[] salt, int iteration, String hashAlgorithm)
				throws NoSuchAlgorithmException, InvalidKeySpecException {
			String generationFactory = CryptoUtils.getPBKDF2KeyGenerationFactory(hashAlgorithm);
			int lenght = CryptoUtils.getKeySizeBits(mAlgorithm, CryptoUtils.detectHashSize(hashAlgorithm));
			CryptoUtils.println("bits", lenght);
			CryptoUtils.println("factory", generationFactory);
			final SecretKeyFactory factory = SecretKeyFactory.getInstance(generationFactory);

			final KeySpec cipherSpec = new PBEKeySpec(password.toCharArray(), salt, iteration, lenght);
			SecretKey secretKey = factory.generateSecret(cipherSpec);
			byte[] encoded = secretKey.getEncoded();
			CryptoUtils.println("pbkf2-key", encoded);

			putKey(secretKey);
			return this;
		}

		public Builder usePassword(String password, String hashAlgorithm) throws NoSuchAlgorithmException {
			CryptoUtils.println("hash", hashAlgorithm);
			MessageDigest instance = MessageDigest.getInstance(hashAlgorithm);
			byte[] hash = instance.digest(password.getBytes());

			int length = CryptoUtils.getKeySizeBytes(mAlgorithm, hash.length);
			CryptoUtils.println("lenght", length*8);
			int oldLenght = hash.length;
			if (oldLenght != length) {
				hash = Arrays.copyOf(hash, length);
			}

			while (oldLenght < length) {
				instance.update(hash, 0, oldLenght);
				byte[] digest = instance.digest();
				System.arraycopy(digest, 0, hash, oldLenght, Math.min(length - oldLenght, digest.length));
				oldLenght = oldLenght + digest.length;
			}

			if (mIvGenerator == null) {
				mIvGenerator = HashIvGenerator.create(hashAlgorithm, hash);
				CryptoUtils.println("iv", mIvGenerator.generateIv(length));
			}

			CryptoUtils.println("hash-key", hash);
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
		public Builder generatePairKey(int keysize) throws NoSuchAlgorithmException {
			KeyPairGenerator generator = KeyPairGenerator.getInstance(mAlgorithm);
			generator.initialize(keysize);
			KeyPair keyPair = generator.generateKeyPair();
			return setKeyPair(keyPair);
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
			return new CryptoConfiguration(algorithm, keys, salt, ivGenerator);
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