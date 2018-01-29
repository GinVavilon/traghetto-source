/**
 *
 */
package com.github.ginvavilon.traghentto.file;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.github.ginvavilon.traghentto.DelegatedSource;
import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.Logger.Level;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class CryptoSource<T extends Source> extends DelegatedSource<T> implements Source {
    
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

        return createKey("AES",key);
        
	}
	
	public static Key createKey(String algorithm, String secret) throws InvalidKeySpecException, NoSuchAlgorithmException {
		
		byte[] bytes = secret.getBytes();
		Logger.d(Level.SOURCE, "Secret size = %s",bytes.length);
		Key secretKeySpec = new SecretKeySpec(bytes,algorithm);
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


}
