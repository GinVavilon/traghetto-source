/**
 *
 */
package com.github.ginvavilon.traghentto.crypto;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import com.github.ginvavilon.traghentto.DelegatedSource;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.crypto.Crypto.Algorithm;
import com.github.ginvavilon.traghentto.crypto.salt.Salt;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class CryptoSource<T extends Source> extends DelegatedSource<T> implements Source, Crypto {
	
	final CryptoConfiguration mConfiguration;

	public CryptoSource(T source, Key key) {
		this(source, new CryptoConfiguration.Builder().setKey(key).build());
	}
    
    public CryptoSource(T source, CryptoConfiguration configuration) {
    	super(source);
    	mConfiguration=configuration;
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
		return new CryptoSource<Source>(source, mConfiguration);
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
		},Salt::removeSalt);
		
	}

	protected<T extends Closeable> StreamResource<T> wrapStreamResource(StreamResource<T> resource, int mode, StreamWrapper<T> wrapper,SaltProcessor<T> saltProcessor)
			throws IOException, IOSourceException {
		T cipherStream = null;
		try {
			Cipher cipher = getCipher(mode);
			T sourceStream = resource.getStream();
			cipherStream = wrapper.wrap(sourceStream, cipher);
			saltProcessor.process(mConfiguration.getSalt(), cipherStream);
			return new CryptoStreamResource<>(resource, cipherStream);
		} catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			resource.close();
			IOSourceException exception = new IOSourceException(e);
			if (cipherStream != null) {
				try {
					cipherStream.close();
				} catch (Exception closeException) {
					exception.addSuppressed(closeException);
				}
			}
			throw exception;
		}
	}

	@Override
	public long getLenght() {
		return UNKNOWN_LENGHT;
	}
	
	protected Cipher getCipher(int encryptMode)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		Cipher cipher = Cipher.getInstance(mConfiguration.getAlgorithm());
		Key key = mConfiguration.getKey();
		
		if (mConfiguration.getAlgorithm().contains(Crypto.Mode.CBC)) {
			byte[] iv = mConfiguration.getIv(cipher.getBlockSize());

			
			IvParameterSpec ivParams = new IvParameterSpec(iv);
			cipher.init(encryptMode, key, ivParams);
		} else {
			cipher.init(encryptMode, key);
		}
		CryptoUtils.println("cipher", cipher.getAlgorithm());
		CryptoUtils.println("cipher-key-format", key.getFormat());
		CryptoUtils.println("cipher-key", key.getEncoded());
		CryptoUtils.println("cipher-iv", cipher.getIV());
		return cipher;
	}


	public String getUriString() {
		return "encrypted-"+mSource.getUriString();
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
	protected interface SaltProcessor<T extends Closeable> {
		void process(Salt salt, T stream) throws IOException;
	}

	

}
