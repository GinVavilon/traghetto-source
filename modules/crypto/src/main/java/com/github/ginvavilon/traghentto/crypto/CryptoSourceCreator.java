package com.github.ginvavilon.traghentto.crypto;

import com.github.ginvavilon.traghentto.SourceCreator;
import com.github.ginvavilon.traghentto.WritableSource;
import com.github.ginvavilon.traghentto.crypto.salt.NoSalt;
import com.github.ginvavilon.traghentto.crypto.salt.Salt;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public final class CryptoSourceCreator<T extends WritableSource>
        implements SourceCreator<EncryptoSource<?>> {

	private SourceCreator<? extends T> mCreator;
	private CryptoConfiguration mCryptoConfiguration;

	public CryptoSourceCreator(SourceCreator<? extends T> creator, Key key) {
		super();
		mCreator = creator;
		mCryptoConfiguration = CryptoConfiguration.builder().setKey(key).build();
	}

	public CryptoSourceCreator(SourceCreator<? extends T> creator, CryptoConfiguration cryptoConfiguration) {
		super();
		mCreator = creator;
		mCryptoConfiguration = cryptoConfiguration;
	}

	@Override
    public EncryptoSource<?> create(String pParam) {
		T source = mCreator.create(pParam);
		return create(source);
	}

    protected EncryptoSource<?> create(T source) {

		Salt salt = mCryptoConfiguration.getSalt();
		if ((salt!=null)&&!(salt instanceof NoSalt)) {
            SaltyWritableSource<T> saltySource = new SaltyWritableSource<T>(source, salt);
            return new EncryptoSource<>(saltySource, mCryptoConfiguration);
		} else {
		    return new EncryptoSource<>(source, mCryptoConfiguration);
		}
	}

	public static <T extends WritableSource> CryptoSourceCreator<T> create(SourceCreator<? extends T> creator,
			String key) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException {
		
		CryptoConfiguration configuration = CryptoConfiguration.builder().setKey(key).build();
		return new CryptoSourceCreator<T>(creator, configuration);

	}

	public static <T extends WritableSource> CryptoSourceCreator<T> createByPassword(SourceCreator<? extends T> creator,
			String password) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException {
		CryptoConfiguration configuration = CryptoConfiguration
				.builder()
				.usePassword(password)
				.addRandomSalt(5,12)
				.build();
		return new CryptoSourceCreator<T>(creator, configuration);
	}
	
    public static <T extends WritableSource> CryptoSourceCreator<T> create(SourceCreator<? extends T> creator,
                                                                           CryptoConfiguration configuration)
            throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException {
		return new CryptoSourceCreator<T>(creator, configuration);
	}

}