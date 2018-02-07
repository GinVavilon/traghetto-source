package com.github.ginvavilon.traghentto.crypto;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.github.ginvavilon.traghentto.crypto.iv.ConstIvGenerator;
import com.github.ginvavilon.traghentto.crypto.iv.EmptyIvGenerator;
import com.github.ginvavilon.traghentto.crypto.iv.HashIvGenerator;
import com.github.ginvavilon.traghentto.crypto.iv.IvGenerator;
import com.github.ginvavilon.traghentto.crypto.salt.NoSalt;
import com.github.ginvavilon.traghentto.crypto.salt.Salt;
import com.github.ginvavilon.traghentto.crypto.salt.SaltFactory;

public class CryptoConfiguration {

	private final Key mKey;

	private final Salt mSalt;

	private final String mAlgorithm;

	private final IvGenerator mIvGenerator;

	private CryptoConfiguration(String algorithm, Key key, Salt salt, IvGenerator ivGenrator) {
		super();
		mAlgorithm = algorithm;
		mKey = key;
		mSalt = salt;
		mIvGenerator = ivGenrator;
	}

	public String getAlgorithm() {
		return mAlgorithm;
	}

	public Salt getSalt() {
		return mSalt;
	}

	public Key getKey() {
		return mKey;
	}

	public byte[] getIv(int blockSize) {
		return mIvGenerator.generateIv(blockSize);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private static final String ALGORITHM_FORMAT = "%s/%s/%s";

		private Key mKey;
		private Salt mSalt = new NoSalt();
		private String mAlgorithm = Crypto.DEFAULT;
		private IvGenerator mIvGenerator;
		private String mMode = Crypto.DEFAULT_MODE;
		private String mPadding = Crypto.DEFAULT_PADDING;

		public Builder setKey(Key key) {
			mKey = key;
			mAlgorithm = key.getAlgorithm();
			return this;
		}

		public Builder setMode(String mode) {
			mMode = mode;
			return this;
		}

		public void setPadding(String padding) {
			mPadding = padding;
		}

		public Builder setKey(byte[] key) {
			Key secretKeySpec = new SecretKeySpec(key, mAlgorithm);
			mKey = secretKeySpec;
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
			mKey = secretKey;
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

			mKey = secretKey;
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
			mKey = new SecretKeySpec(hash, mAlgorithm);

			return this;

		}

		public CryptoConfiguration build() {

			Key key = this.mKey;
			String algorithm = String.format(ALGORITHM_FORMAT, mAlgorithm, mMode, mPadding);
			Salt salt = mSalt;
			IvGenerator ivGenerator = mIvGenerator;
			if (ivGenerator == null) {
				ivGenerator = new EmptyIvGenerator();
			}
			return new CryptoConfiguration(algorithm, key, salt, ivGenerator);
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