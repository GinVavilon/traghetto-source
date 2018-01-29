//Copyright © 2018 M800 Limited. All rights reserved.
package com.github.ginvavilon.traghentto.file;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64.Decoder;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.StreamUtils;
import com.github.ginvavilon.traghentto.WritableSource;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;
import com.github.ginvavilon.traghentto.params.VoidParams;

public class EncryptoSource<T extends WritableSource> extends CryptoSource<T> implements WritableSource {

	public EncryptoSource(T source, Key key) {
		super(source, key);
	}

	@Override
	public boolean delete() {
		return getSource().delete();
	}

	@Override
	protected CryptoSource<? extends Source> wrapChild(Source source) {
		return new EncryptoSource<WritableSource>((WritableSource) source, mKey);
	}


	@Override
	public boolean create() throws IOException {
		return getSource().create();
	}

	@Override
	public boolean createConteiner() throws IOException {
		return getSource().create();
	}

	@Override
	public EncryptoSource<WritableSource> getChild(String name) {
		return new EncryptoSource<>(getSource().getChild(name), mKey);
	}

	@Override
	public StreamResource<OutputStream> openOutputResource(StreamParams pParams) throws IOException, IOSourceException {
		StreamResource<OutputStream> resource = mSource.openOutputResource(pParams);
		return wrapStreamResource(resource, Cipher.ENCRYPT_MODE,new StreamWrapper<OutputStream>() {

			@Override
			public OutputStream wrap(OutputStream stream, Cipher cipher) {
				return new CipherOutputStream(stream, cipher);
			}
		});
	}

	@Override
	public StreamResource<OutputStream> openOutputResource() throws IOException, IOSourceException {
		return openOutputResource(new VoidParams());
	}

	

}
