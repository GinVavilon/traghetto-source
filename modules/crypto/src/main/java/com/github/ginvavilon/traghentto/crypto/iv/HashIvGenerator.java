package com.github.ginvavilon.traghentto.crypto.iv;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class HashIvGenerator extends ByteArrayIvGenerator {

	private MessageDigest mMessageDigest;
	
	public HashIvGenerator(MessageDigest messageDigest) {
		super();
		mMessageDigest = messageDigest;
	}

	public static final HashIvGenerator create(String hash, byte[] initial) throws NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance(hash);
		messageDigest.update(initial);
		return  new HashIvGenerator(messageDigest);
	}

	@Override
	protected byte[] generateMore(byte[] bytes, int size) {

		int add = size % mMessageDigest.getDigestLength();
		if (add > 0) {
			add = mMessageDigest.getDigestLength() - add;
		}
		
		int newSize = size +add;
		
		byte[] result = Arrays.copyOf(bytes, newSize);
		
		byte[] digest = mMessageDigest.digest();
		System.arraycopy(digest, 0, result, bytes.length, Math.min(digest.length, newSize - bytes.length));
		return result;
	}

}
