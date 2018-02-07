package com.github.ginvavilon.traghentto.crypto.iv;

import java.util.Arrays;

public abstract class ByteArrayIvGenerator implements IvGenerator {

	private byte[] mBytes;
	
	
	public ByteArrayIvGenerator(byte[] bytes) {
		super();
		mBytes = bytes;
	}
	public ByteArrayIvGenerator() {
		super();
		mBytes = new byte[0];
	}

	@Override
	public byte[] generateIv(int size) {

		if (mBytes.length < size) {
			mBytes = generateMore(mBytes, size);
		}
		return Arrays.copyOf(mBytes, size);
	}

	protected abstract byte[] generateMore(byte[] bytes, int size);

}
