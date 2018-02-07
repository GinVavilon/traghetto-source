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

	
}
