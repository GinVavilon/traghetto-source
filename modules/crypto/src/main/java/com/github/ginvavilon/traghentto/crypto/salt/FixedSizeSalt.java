package com.github.ginvavilon.traghentto.crypto.salt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FixedSizeSalt extends BaseRandomSalt implements Salt {

	private final int mSize;
	
	public FixedSizeSalt(int size) {
		super();
		mSize = size;
	}

	@Override
	protected int getSize(InputStream inputStream) throws IOException {
		int skiped = (int) inputStream.skip(mSize);
		int left =mSize - skiped;
		return left;
	}

	@Override
	protected int calulateSize(OutputStream outputStream) throws IOException {
		return mSize;
	}



}
