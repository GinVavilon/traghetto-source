package com.github.ginvavilon.traghentto.file;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;

import org.junit.Test;

public class SaltRandomTest {

	private static final int SIZE_BUFFER_LENGTH = 4 * 3;
	private static final int MASK = 1 << 31;
	private int mMinSize = 5;
	private int mMaxSize = 12;

	@Test
	public void test() {
		Random random = new SecureRandom();
		assertParam(random, 0, 0);
		assertParam(random, 0xFFFFFFFF, 0xFFFFFFFF);
		assertParam(random, 0xFFFFFFFF, 0);
		assertParam(random, 0, 0xFFFFFFFF);
		
		assertParam(random, 0, 0);
		assertParam(random, -1, -1);
		assertParam(random, -1, 0);
		assertParam(random, 0, -1);
		
		assertParam(random, Integer.MIN_VALUE,Integer.MAX_VALUE);
		assertParam(random, 0,Integer.MAX_VALUE);
		assertParam(random, Integer.MIN_VALUE,0);
		
		
		for (int i = 0; i < 10000000; i++) {

			int param1 = random.nextInt();
			int param2 = random.nextInt();
			
			assertParam(random, param1, param2);
		}

	}

	private void assertParam(Random random, int param1, int param2) {
		int randomX1 = param1 & (~MASK);
		int randomX2 = param2 & (~MASK);
		
		int size = mMinSize + random.nextInt(mMaxSize - mMinSize);
		int m1 = randomX1 + size;
		int m2 = randomX2 + size;
		int m3 = randomX2 + randomX1;

		ByteBuffer outBuffer = ByteBuffer.allocate(SIZE_BUFFER_LENGTH);
		outBuffer.putInt(m3).putInt(m2).putInt(m1);

		ByteBuffer inBuffer = ByteBuffer.allocate(SIZE_BUFFER_LENGTH);
		System.arraycopy(outBuffer.array(), 0, inBuffer.array(), 0, SIZE_BUFFER_LENGTH);

		int e3 = inBuffer.getInt();
		int e2 = inBuffer.getInt();
		int e1 = inBuffer.getInt();
		int eSize = (e1 - e3 + e2) >> 1;

		assertEquals(m3, e3);
		assertEquals(m2, e2);
		assertEquals(m1, e1);
		assertEquals(size, eSize);
	}

}
