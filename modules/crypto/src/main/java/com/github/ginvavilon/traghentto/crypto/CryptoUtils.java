package com.github.ginvavilon.traghentto.crypto;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.github.ginvavilon.traghentto.crypto.Crypto.Algorithm;
import com.github.ginvavilon.traghentto.crypto.Crypto.Hash;

public class CryptoUtils {

	private static final String PBKDF2_WITH = "PBKDF2With";
	private static final Map<String, KeySize> KEY_SIZES = new HashMap<>();
	static {
		KEY_SIZES.put(Algorithm.AES, new ListBitsKeySize(128, 192, 256));
		KEY_SIZES.put(Algorithm.DES, new ListBitsKeySize(64));
		KEY_SIZES.put(Algorithm.RSA, new ListBitsKeySize(1024, 2048, 4096));
		KEY_SIZES.put(Algorithm.DES_EDE, new ListBitsKeySize(128, 192));
		KEY_SIZES.put(Algorithm.RC2, new ListBitsKeySize(128));
		KEY_SIZES.put(Algorithm.BLOWFISH, new LimitKeySize(32, 448));
	}
	
	
	static void println(String string, byte[] hash) {
		System.out.print(String.format("%12s - ", string));
		for (byte b : hash) {
			System.out.print(String.format("%02X", b));
			System.out.print(":");
	
		}
		System.out.println();
	
	}

	static void println(String string, Object message) {
		System.out.print(String.format("%12s - ", string));
		System.out.print(message);
		System.out.println();
	}


	public static int getKeySizeBytes(String algorithm, int baselength) {

		return getKeySizeBits(algorithm, baselength * 8) / 8;

	}

	public static int getKeySizeBits(String algorithm, int baselength) {

		KeySize size = KEY_SIZES.get(algorithm);

		int length = baselength;
		if (size != null) {
			length = size.changeSize(length);
		}
		return length;

	}

	interface KeySize {
		int changeSize(int oldSize);
	}

	private static class ListBitsKeySize implements KeySize {

		private final int[] mSizes;

		public ListBitsKeySize(int... sizes) {
			super();

			mSizes = new int[sizes.length];
			for (int i = 0; i < sizes.length; i++) {
				mSizes[i] = sizes[i];
			}
			Arrays.sort(mSizes);
		}

		@Override
		public int changeSize(int oldSize) {
			int lastSize = mSizes[0];
			for (int size : mSizes) {
				if (size > oldSize) {
					return lastSize;
				}
				lastSize = size;
			}
			return lastSize;
		}

	}

	private static class LimitKeySize implements KeySize {

		private final int mMinSize;
		private final int mMaxSize;

		public LimitKeySize(int minSize, int maxSize) {
			super();
			mMinSize = minSize;
			mMaxSize = maxSize;
		}

		@Override
		public int changeSize(int oldSize) {
			if (oldSize < mMinSize) {
				return mMinSize;
			}
			if (oldSize > mMaxSize) {
				return mMaxSize;
			}
			return oldSize;
		}

	}

	private static String getHmackHash(String hashAlgorithm) {
		if (hashAlgorithm.startsWith("SHA")) {
			return "Hmac" + hashAlgorithm.replaceAll("-", "");

		}
		return hashAlgorithm;
	}

	public static String getPBEKeyGenerationFactory(String hashAlgorithm, String algorithm) {
		if (hashAlgorithm.startsWith("PBEWith")) {
			return hashAlgorithm;
		}
		return String.format("PBEWith%sAnd%s", getHmackHash(hashAlgorithm),
				getKeyGenerationAlgorithm(algorithm, hashAlgorithm));
	}

	public static String getPBKDF2KeyGenerationFactory(String hashAlgorithm) {
		if (hashAlgorithm.startsWith(PBKDF2_WITH)) {
			return hashAlgorithm;
		}
		return PBKDF2_WITH + getHmackHash(hashAlgorithm);
	}

	private static String getKeyGenerationAlgorithm(String algorithm, String hashAlgorithm) {
		switch (algorithm) {
		case Algorithm.AES:
			int bits = detectHashSize(hashAlgorithm);
			return "AES_" + getKeySizeBits(algorithm, bits);
		default:
			return algorithm;
		}
	}

	public static int detectHashSize(String hashAlgorithm) {
		switch (hashAlgorithm) {
		case Hash.MD5:
			return 128;
		case Hash.SHA1:
			return 160;
		case Hash.SHA512:
			return 512;
		case Hash.SHA256:
			return 256;
		case Hash.SHA224:
			return 224;
		case Hash.MD2:
			return 128;
		case "PBKDF2WithHmacSHA256":
			return 256;
		case "PBKDF2WithHmacSHA1":
			return 160;
		case "PBKDF2WithHmacSHA224":
			return 224;
		case "PBKDF2WithHmacSHA512":
			return 512;
		default:
			return 0;
		}
	}

}
