package com.github.ginvavilon.traghentto.crypto.salt;

import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Random;

public abstract class BaseRandomSalt extends BaseSalt implements Salt{

	private Random mRandom;

	protected abstract int calulateSize(OutputStream outputStream) throws IOException;

	public BaseRandomSalt() {
		super();
		mRandom = new SecureRandom();
	}
	
	protected Random getRandom() {
		return mRandom;
	}

	@Override
	public void addSalt(OutputStream outputStream) throws IOException {
		int left = calulateSize(outputStream);
		int bufferSize = left > BaseSalt.BUFFER_SIZE ? BaseSalt.BUFFER_SIZE : left;
		byte[] buffer = new byte[bufferSize];
		while (left > 0) {
			mRandom.nextBytes(buffer);
			outputStream.write(buffer, 0, (int) Math.min(bufferSize, left));
			left -= bufferSize;
		}
		outputStream.flush();
	}

}