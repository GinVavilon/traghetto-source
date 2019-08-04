package com.github.ginvavilon.traghentto.crypto.iv;

public class ConstIvGenerator extends ByteArrayIvGenerator {

	public ConstIvGenerator(byte[] iv) {
		super(iv);
	}

	@Override
	protected byte[] generateMore(byte[] bytes, int size) {
		throw new IllegalArgumentException("Size " + size + " has more than" + bytes.length);
	}

}
