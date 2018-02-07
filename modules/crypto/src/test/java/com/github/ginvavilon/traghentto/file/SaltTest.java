package com.github.ginvavilon.traghentto.file;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.junit.Test;

import com.github.ginvavilon.traghentto.crypto.salt.Salt;
import com.github.ginvavilon.traghentto.crypto.salt.SaltFactory;

public class SaltTest {

	private static final int STEPS = 100000;
	private static final byte[] TEST_MESSAGE = "Test".getBytes();
	private static final byte[] TEST_SALT = "Salts".getBytes();

	@Test
	public void testNoSalt() throws Exception {
		testSalt(() -> SaltFactory.createNoSalt());
	}

	@Test
	public void testFixedRandomSalt() throws Exception {
		testSalt(() -> SaltFactory.createRandomWithFixedSize(1024));
	}

	@Test
	public void testRandomSalt() throws Exception {
		testSalt(() -> SaltFactory.createRandom(1024));
		testSalt(() -> SaltFactory.createRandom(0, 1024));
	}

	@Test
	public void testConstSalt() throws Exception {
		testSalt(() -> SaltFactory.createSalt(TEST_SALT));
	}

	public void testSalt(Callable<Salt> creator) throws Exception {
		for (int i = 0; i < STEPS; i++) {
			assertParam(creator);
		}
	}

	private void assertParam(Callable<Salt> creator) throws Exception {
		byte[] result = addSalt(creator, TEST_MESSAGE);
		byte[] buf = removeSalt(creator, result);
		assertArrayEquals(TEST_MESSAGE, buf);
	}

	private byte[] addSalt(Callable<Salt> creator, byte[] testMessage) throws Exception, IOException {
		Salt randomSalt = creator.call();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		randomSalt.addSalt(outputStream);

		outputStream.write(testMessage, 0, testMessage.length);

		byte[] result = outputStream.toByteArray();
		return result;
	}

	private byte[] removeSalt(Callable<Salt> creator, byte[] result) throws Exception, IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(result);
		Salt removeSalt = creator.call();

		removeSalt.removeSalt(inputStream);
		int available = inputStream.available();
		byte[] buf = new byte[available];
		inputStream.read(buf, 0, available);
		return buf;
	}

}
