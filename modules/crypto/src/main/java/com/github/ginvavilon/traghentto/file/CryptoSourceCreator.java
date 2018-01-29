package com.github.ginvavilon.traghentto.file;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.github.ginvavilon.traghentto.SourceCreator;
import com.github.ginvavilon.traghentto.WritableSource;

public final class CryptoSourceCreator<T extends WritableSource> implements SourceCreator<EncryptoSource<T>> {

	private SourceCreator<? extends T> mCreator;
	private Key mKey;

	public CryptoSourceCreator(SourceCreator<? extends T> creator, Key key) {
		super();
		mCreator = creator;
		mKey = key;
	}

	@Override
	public EncryptoSource<T> create(String pParam) {
		T source = mCreator.create(pParam);
		return create(source);
	}

	protected EncryptoSource<T> create(T source) {
		return new EncryptoSource<T>(source, mKey);
	}

	public static <T extends WritableSource > CryptoSourceCreator<T> create(SourceCreator<? extends T> creator, String key)
			throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException {
		Key secretKey = CryptoSource.createKey(key);
		return new CryptoSourceCreator<T>(creator, secretKey);

	}
}