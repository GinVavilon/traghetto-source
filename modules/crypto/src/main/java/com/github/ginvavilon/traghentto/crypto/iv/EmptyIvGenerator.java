package com.github.ginvavilon.traghentto.crypto.iv;

public class EmptyIvGenerator implements IvGenerator{

	@Override
	public byte[] generateIv(int size) {
		return new byte[size];
	}

}
