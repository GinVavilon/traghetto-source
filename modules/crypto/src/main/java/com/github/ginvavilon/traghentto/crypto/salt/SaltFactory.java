package com.github.ginvavilon.traghentto.crypto.salt;

public class SaltFactory {
	
	public static Salt createNoSalt() {
		return new NoSalt();
	}
	
	public static Salt createRandomWithFixedSize(int size) {
		return new FixedSizeSalt(size);
	}
	
	public static Salt createRandom(int size) {
		return new RandomSalt(size,size);
	}
	
	public static Salt createRandom(int min, int max) {
		return new RandomSalt(min, max);
	}

	public static Salt createSalt(byte[] salt) {
		return new ByteSalt(salt);
	}

}
