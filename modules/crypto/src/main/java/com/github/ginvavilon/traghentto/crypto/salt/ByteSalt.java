package com.github.ginvavilon.traghentto.crypto.salt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ByteSalt extends BaseSalt {

	private byte[] mSalt;

	public ByteSalt(byte[] salt) {
		mSalt = salt;
	}

	@Override
	public void addSalt(OutputStream outputStream) throws IOException {
		outputStream.write(mSalt);
	}


	@Override
	protected int getSize(InputStream inputStream) throws IOException {
		int size=mSalt.length;
		int skiped = (int) inputStream.skip(size);
		int left =size - skiped;
		return left;
	}

}
