package com.github.ginvavilon.traghentto.crypto.salt;

import java.io.InputStream;
import java.io.OutputStream;

public class NoSalt implements Salt {

	@Override
	public void removeSalt(InputStream inputStream) {
	}

	@Override
	public void addSalt(OutputStream outputStream) {
	}

}
