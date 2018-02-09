package com.github.ginvavilon.traghentto.crypto;

import com.github.ginvavilon.traghentto.crypto.salt.NoSalt;

public interface Crypto {

	public static final String DEFAULT = Algorithm.AES;
	public static final String DEFAULT_HASH = Hash.SHA256;
	public static final String DEFAULT_MODE = Mode.CBC;
	public static final String DEFAULT_PADDING = Padding.PKCS5;
	public static final NoSalt NO_SALT = new NoSalt();
	
	public interface Algorithm{
		String AES = "AES";
		String AES_WRAP = "AESWrap";
		String ARCFOUR = "ARCFOUR";
		String BLOWFISH = "Blowfish";
		String CCM = "CCM";
		String DES = "DES";
		String DES_EDE = "DESede";
		String DES_EDE_WRAP = "DESedeWrap";
		String ECIES = "ECIES";
		String GCM = "GCM";
		String RC2 = "RC2";
		String RSA = "RSA";
	}
	
	public interface Hash{
		String MD2 = "MD2";
		String MD5 = "MD5";
		String SHA1 = "SHA-1";
		String SHA224 = "SHA-224";
		String SHA256 = "SHA-256";
		String SHA512 = "SHA-512";
	}
	
	public interface Mode{
		public static final String CBC = "CBC";
		public static final String CFB = "CFB";
		public static final String CTR = "CTR";
		public static final String CTS = "CTS";
		public static final String ECB = "ECB";
		public static final String NONE = "NONE";
		public static final String OFB = "OFB";
		public static final String PCBC = "PCBC";
	}

	public interface Padding{
		public static final String ISO10126 = "ISO10126Padding";
		public static final String NO = "NoPadding";
		public static final String OAEP = "OAEPPadding";
		public static final String PKCS1 = "PKCS1Padding";
		public static final String PKCS5 = "PKCS5Padding";
		public static final String SSL3 = "SSL3Padding";
	}
	
	public interface KeySize{

		int BLOWFISH_MAX = 448;
		int BLOWFISH_MIN = 32;
		int RC2 = 128;
		int DES_EDE_192 = 192;
		int DES_EDE_128 = 128;
		int RSA_4096 = 4096;
		int RSA_2048 = 2048;
		int RSA_1024 = 1024;
		int DES = 64;
		int AES_256 = 256;
		int AES_192 = 192;
		int AES_128 = 128;
        int DES_EDE_192_GENERATED = 128;
        int DES_EDE_128_GENERATED = 112;
        int DES_GENERATED = 56;
		
	}

	
}
