package com.github.ginvavilon.traghentto.crypto.salt;

import java.io.IOException;
import java.io.InputStream;

public abstract class BaseSalt implements Salt{

	protected static final int BUFFER_SIZE = 512;

	public BaseSalt() {
		super();
	}

	protected abstract int getSize(InputStream inputStream) throws IOException;

	@Override
	public void removeSalt(InputStream inputStream) throws IOException {
		int left = getSize(inputStream);
		int bufferSize = left > BaseSalt.BUFFER_SIZE ? BaseSalt.BUFFER_SIZE : left;
		byte[] buffer = new byte[bufferSize];
		while (left > 0) {
			left -= inputStream.read(buffer, 0, (int) Math.min(bufferSize, left));
		}
	}

}