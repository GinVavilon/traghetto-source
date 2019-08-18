package com.github.ginvavilon.traghentto.crypto;

import com.github.ginvavilon.traghentto.DelegatedSource;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class CryptoSource<T extends Source> extends DelegatedSource<T> implements Source {

    final CipherProvider mCipherProvider;

    CryptoSource(T source, Key key) {
        this(source, new CryptoConfiguration.Builder().setKey(key).build());
    }

    CryptoSource(T source, CipherProvider cipherProvider) {
        super(source);
        mCipherProvider = cipherProvider;
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
        return new CryptoSource<>(source, mCipherProvider);
    }

	@Override
    public Source getChild(String name) {
		return wrapChild(getSource().getChild(name));
	}



    @Override
    public StreamResource<InputStream> openResource(StreamParams pParams)
            throws IOSourceException, IOException {
        StreamResource<InputStream> resource = getSource().openResource(pParams);
        return wrapStreamResource(resource, Cipher.DECRYPT_MODE, CipherInputStream::new);

    }

    protected <T extends Closeable> StreamResource<T> wrapStreamResource(StreamResource<T> resource,
            int mode, StreamWrapper<T> wrapper)
            throws IOSourceException {
        Cipher cipher;
        try {
            cipher = getCipher(mode);
        } catch (UnavailableCipherException e) {
            throw new IOSourceException(e);
        }

        T sourceStream = resource.getStream();
        T cipherStream = wrapper.wrap(sourceStream, cipher);
        return new CryptoStreamResource<>(resource, cipherStream);

    }

    @Override
    public long getLenght() {
        return UNKNOWN_LENGHT;
    }

    protected Cipher getCipher(int mode) throws UnavailableCipherException {
        return mCipherProvider.getCipher(mode);
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

    @Override
    public String toString() {
        return "Crypted!" + getSource();
    }

}
