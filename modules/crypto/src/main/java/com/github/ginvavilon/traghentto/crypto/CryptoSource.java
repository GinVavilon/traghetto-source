/**
 *
 */
package com.github.ginvavilon.traghentto.crypto;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import com.github.ginvavilon.traghentto.DelegatedSource;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.StreamResource;
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


	@Override
    public List<? extends Source> getChildren() {
		List<Source> list = new ArrayList<>();
		List<? extends Source> children = getSource().getChildren();
        if (children == null) {
            return null;
        }
		for (Source source : children) {
			Source cryptoSource = wrapChild(source);
			list.add(cryptoSource);
		}
		return list;
	}

	protected CryptoSource<? extends Source> wrapChild(Source source) {
		return new CryptoSource<Source>(source, mConfiguration);
	}

	@Override
    public Source getChild(String name) {
		return wrapChild(getSource().getChild(name));
	}


	
	@Override
	public StreamResource<InputStream> openResource(StreamParams pParams) throws IOSourceException, IOException {
		StreamResource<InputStream> resource = getSource().openResource(pParams);
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
		Key key = mConfiguration.getKey(encryptMode);
		byte[] iv = mConfiguration.getIv(cipher.getBlockSize());
        if (iv != null) {
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(encryptMode, key, ivParams);
        } else {
            cipher.init(encryptMode, key);

        }
		SecurityLogger.println("cipher", cipher.getAlgorithm());
		SecurityLogger.println("cipher-key-format", key.getFormat());
		SecurityLogger.println("cipher-key", key.getEncoded());
		SecurityLogger.println("cipher-iv", cipher.getIV());
		return cipher;
	}


	@Override
    public String getUriString() {
		return "encrypted-"+getSource().getUriString();
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

    @Override
    public String toString() {
        return "Crypted!" + getSource();
    }

}
