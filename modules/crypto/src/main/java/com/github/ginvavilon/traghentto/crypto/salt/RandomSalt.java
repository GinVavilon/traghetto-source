package com.github.ginvavilon.traghentto.crypto.salt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

public class RandomSalt extends BaseRandomSalt {

	private static final int SIZE_BUFFER_LENGTH = 4 * 3;
	private static final int MASK = 1 << 31;
	private final int mMinSize;
	private final int mMaxSize;

	public RandomSalt(int minSize, int maxSize) {
		super();
		mMinSize = minSize;
		mMaxSize = maxSize;
	}

	@Override
	protected int calculateSize(OutputStream outputStream) throws IOException {

		Random random = getRandom();
		int randomX1 = random.nextInt()&(~MASK);
		int randomX2 = random.nextInt()&(~MASK);
		int size;
		if (mMaxSize > mMinSize) {
			size = mMinSize + random.nextInt(mMaxSize - mMinSize);
		} else {
			size = mMaxSize;
		}

		int m1 = randomX1 + size;
		int m2 = randomX2 + size;
		int m3 = randomX2 + randomX1;

		ByteBuffer buffer = ByteBuffer.allocate(SIZE_BUFFER_LENGTH);
		buffer.putInt(m3).putInt(m2).putInt(m1);

		outputStream.write(buffer.array());
		outputStream.flush();
		return size;
	}

	@Override
	protected int getSize(InputStream inputStream) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(SIZE_BUFFER_LENGTH);
		inputStream.read(buffer.array());
		
		int m3 = buffer.getInt();
		int m2 = buffer.getInt();
		int m1 = buffer.getInt();
		int size = (m1-m3+m2)>>1;
		
		return size;
	}



}
