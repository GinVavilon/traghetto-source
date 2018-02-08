package com.github.ginvavilon.traghentto.crypto;

import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.StreamUtils;
import com.github.ginvavilon.traghentto.WritableSource;
import com.github.ginvavilon.traghentto.crypto.Crypto.Algorithm;
import com.github.ginvavilon.traghentto.crypto.Crypto.Hash;
import com.github.ginvavilon.traghentto.crypto.Crypto.KeySizes;
import com.github.ginvavilon.traghentto.crypto.Crypto.Mode;
import com.github.ginvavilon.traghentto.crypto.iv.DisabledIvGenerator;
import com.github.ginvavilon.traghentto.crypto.iv.EmptyIvGenerator;
import com.github.ginvavilon.traghentto.crypto.iv.IvGenerator;

public class CryptoUtils {

	private static final String PBKDF2_WITH = "PBKDF2With";
	private static final Map<String, KeySize> KEY_SIZES = new HashMap<>();
	static {
		KEY_SIZES.put(Algorithm.AES, new ListBitsKeySize(KeySizes.AES_128, KeySizes.AES_192, KeySizes.AES_256));
		KEY_SIZES.put(Algorithm.DES, new ListBitsKeySize(KeySizes.DES));
		KEY_SIZES.put(Algorithm.RSA, new ListBitsKeySize(KeySizes.RSA_1024, KeySizes.RSA_2048, KeySizes.RSA_4096));
		KEY_SIZES.put(Algorithm.DES_EDE, new ListBitsKeySize(KeySizes.DES_EDE_128, KeySizes.DES_EDE_192));
		KEY_SIZES.put(Algorithm.RC2, new ListBitsKeySize(KeySizes.RC2));
		KEY_SIZES.put(Algorithm.BLOWFISH, new LimitKeySize(KeySizes.BLOFISH_MIN, KeySizes.BLOWFISH_MAX));
	}
	
	
	static void println(String string, byte[] hash) {
		System.out.print(String.format("%12s - ", string));
        if (hash == null) {
            System.out.println("null");
            return;
        }
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

	public static KeyPair loadKeys(String algorithm, Source privateKeySouce, Source publicKeySouce)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

		byte[] encodedPrivateKey = StreamUtils.readSource(privateKeySouce);
		byte[] encodedPublicKey = StreamUtils.readSource(publicKeySouce);

		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

		return new KeyPair(publicKey, privateKey);
	}

    public static Key loadPrivateKey(String algorithm, Source privateKeySouce)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] encodedPrivateKey = StreamUtils.readSource(privateKeySouce);

        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        return privateKey;
    }

    public static Key loadPublicKey(String algorithm, Source publicKeySouce)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {

        byte[] encodedPublicKey = StreamUtils.readSource(publicKeySouce);

        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        return publicKey;
    }

	public static boolean savePrivateKey(Key privateKey, WritableSource privateKeySouce) {

		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
		return StreamUtils.writeSource(privateKeySouce, keySpec.getEncoded());

	}

	public static boolean savePublicKey(Key publicKey, WritableSource publicKeySouce) {
		
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey.getEncoded());
		return StreamUtils.writeSource(publicKeySouce, keySpec.getEncoded());
		
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

	static String getPBEKeyGenerationFactory(String hashAlgorithm, String algorithm) {
		if (hashAlgorithm.startsWith("PBEWith")) {
			return hashAlgorithm;
		}
		return String.format("PBEWith%sAnd%s", getHmackHash(hashAlgorithm),
				getKeyGenerationAlgorithm(algorithm, hashAlgorithm));
	}

	static String getPBKDF2KeyGenerationFactory(String hashAlgorithm) {
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

	static int detectHashSize(String hashAlgorithm) {
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

    public static IvGenerator createEmptyIvGenerator(String mode) {
        switch (mode) {
            case Mode.ECB:
                return new DisabledIvGenerator();
            case Mode.CBC:
            case Mode.PCBC:
            case Mode.OFB:
            case Mode.CFB:
                return new EmptyIvGenerator();
            default:
                return new DisabledIvGenerator();
        }
    }

    public static String getDefaultMode(String algorithm) {
        switch (algorithm) {
            case Algorithm.RSA:
                return Crypto.Mode.ECB;
            default:
                return Crypto.DEFAULT_MODE;
        }
    }

    public static String getDefaultPadding(String algorithm) {
        switch (algorithm) {
            case Algorithm.RSA:
                return Crypto.Padding.PKCS1;
            default:
                return Crypto.DEFAULT_PADDING;
        }
    }

}
