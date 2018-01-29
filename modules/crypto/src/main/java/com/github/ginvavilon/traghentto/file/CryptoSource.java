/**
 *
 */
package com.github.ginvavilon.traghentto.file;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

import com.github.ginvavilon.traghentto.DelegatedSource;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class CryptoSource<T extends Source> extends DelegatedSource<T> implements Source {
    
	public static final String SHA1 = "SHA-1";
	public static final String SHA224 = "SHA-224";
	public static final String SHA256 = "SHA-256";
	public static final String SHA512 = "SHA-512";
	
	public static final String MD5 = "MD5";
	public static final String MD2 = "MD2";
	
	public static final String GCM = "GCM";
	public static final String RSA = "RSA";
	public static final String ECIES = "ECIES";
	public static final String DES_EDE_WRAP = "DESedeWrap";
	public static final String DES_EDE = "DESede";
	public static final String DES = "DES";
	public static final String CCM = "CCM";
	public static final String ARCFOUR = "ARCFOUR";
	public static final String AES_WRAP = "AESWrap";
	public static final String AES = "AES";
	public static final String BLOWFISH = "Blowfish";
	public static final String RC2 = "RC2";
	public static final String DEFAULT = DES;
	private static final String DEFAULT_HASH = SHA1;
	private static final Map<String,KeySize> KEY_SIZES=new HashMap<>();
	static {
		KEY_SIZES.put(AES, new ListBitsKeySize(128, 192, 256));
		KEY_SIZES.put(DES, new ListBitsKeySize(64));
		KEY_SIZES.put(RSA, new ListBitsKeySize(1024, 2048, 4096));
		KEY_SIZES.put(DES_EDE, new ListBitsKeySize(112, 168));
		KEY_SIZES.put(RC2, new ListBitsKeySize(128));
		KEY_SIZES.put(BLOWFISH, new LimitKeySize(32, 448));
	}

	Key mKey;
	
    public CryptoSource(T source, Key key) {
		super(source);
		this.mKey = key;
	}

    protected T getSource() {
		return mSource;
	}
    
	public List<? extends Source> getChildren() {
		List<Source> list = new ArrayList<>();
		List<? extends Source> children = mSource.getChildren();
		for (Source source : children) {
			Source cryptoSource = wrapChild(source);
			list.add(cryptoSource);
		}
		return list;
	}

	protected CryptoSource<? extends Source> wrapChild(Source source) {
		return new CryptoSource<Source>(source, mKey);
	}

	public Source getChild(String name) {
		return wrapChild(mSource.getChild(name));
	}


	
	@Override
	public StreamResource<InputStream> openResource(StreamParams pParams) throws IOSourceException, IOException {
		StreamResource<InputStream> resource = mSource.openResource(pParams);
		return wrapStreamResource(resource, Cipher.DECRYPT_MODE, new StreamWrapper<InputStream>() {
			
			@Override
			public InputStream wrap(InputStream stream, Cipher cipher) {
				return new CipherInputStream(stream, cipher);
			}
		});
		
	}

	protected<T extends Closeable> StreamResource<T> wrapStreamResource(StreamResource<T> resource, int mode, StreamWrapper<T> wrapper)
			throws IOException, IOSourceException {
		try {
			Cipher cipher = getCipher(mode);
			T sourceStream = resource.getStream();
			T cipherStream = wrapper.wrap(sourceStream, cipher);
			return new CryptoStreamResource<>(resource, cipherStream);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			resource.close();
			throw new IOSourceException(e);
		}
	}


	protected Cipher getCipher(int encryptMode)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		Cipher cipher = Cipher.getInstance(mKey.getAlgorithm());
		cipher.init(encryptMode, mKey);
		return cipher;
	}


	public String getUriString() {
		return "encrypted-"+mSource.getUriString();
	}



	public static Key createKey(String key) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException {

        return createKey(DEFAULT,key);
        
	}
	
	public static Key createKeyByPassword(String algorithm, String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
		
		return createKeyByPassword(algorithm, password, DEFAULT_HASH);
	}
	public static Key createKeyByPassword(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
		
		return createKeyByPassword(DEFAULT, password, DEFAULT_HASH);
	}
	
	public static Key createKeyByPassword(String algorithm, String password,String hashAlgorithm) throws InvalidKeySpecException, NoSuchAlgorithmException {
		MessageDigest instance = MessageDigest.getInstance(hashAlgorithm);
		byte[] hash = instance.digest(password.getBytes());
		
		KeySize size = KEY_SIZES.get(algorithm);
		int length = hash.length;
		if (size != null) {
			length = size.changeSize(length);
		}
		if (hash.length!=length) {
			hash=Arrays.copyOf(hash, length);
		}
		return createKey(algorithm, hash);
	}

	public static Key createKey(String algorithm, String secret) throws InvalidKeySpecException, NoSuchAlgorithmException {
		byte[] bytes = secret.getBytes();
		return createKey(algorithm, bytes);
	}

	protected static Key createKey(String algorithm, byte[] bytes) {
		Key secretKeySpec = new SecretKeySpec(bytes, algorithm);
		return secretKeySpec;
	}
	
	private final class CryptoStreamResource<T extends Closeable> implements StreamResource<T> {
		private final StreamResource<T> mResource;
		private final T mCipherStream;

		private CryptoStreamResource(StreamResource<T> resource,
				T cipherInputStream) {
			mResource = resource;
			mCipherStream = cipherInputStream;
		}

		@Override
		public void close() throws IOException {
			mCipherStream.close();
			mResource.close();
		}

		@Override
		public T takeStream() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Stream must be closed by resource");
		}

		@Override
		public boolean isOpened() {
			return true;
		}

		@Override
		public T getStream() {
			return mCipherStream;
		}
	}
	
	protected interface StreamWrapper<T extends Closeable> {
		T wrap(T stream, Cipher cipher);
	}
	
	private interface KeySize{
		int changeSize(int oldSize);
	}
	

	private static class ListBitsKeySize implements KeySize{

		private final int[] mSizes;
		

		public ListBitsKeySize(int... sizes) {
			super();
			
			mSizes = new int [sizes.length];
			for (int i = 0; i < sizes.length; i++) {
				mSizes[i]=sizes[i]/8;
			}
			Arrays.sort(mSizes);
		}


		@Override
		public int changeSize(int oldSize) {
			int lastSize=mSizes[0];
			for (int size : mSizes) {
				if (oldSize>size) {
					return lastSize;
				}
			}
			return lastSize;
		}
		
	}
	private static class LimitKeySize implements KeySize{
		
		private final int mMinSize;
		private final int mMaxSize;
		
		
		
		public LimitKeySize(int minSize, int maxSize) {
			super();
			mMinSize = minSize/8;
			mMaxSize = maxSize/8;
		}



		@Override
		public int changeSize(int oldSize) {
			if (oldSize<mMinSize) {
				return mMinSize;
			}
			if (oldSize>mMaxSize) {
				return mMaxSize;
			}
			return oldSize;
		}
		
	}
	

}
