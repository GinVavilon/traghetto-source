package com.github.ginvavilon.traghentto.crypto.salt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Salt {
	
	void removeSalt(InputStream inputStream) throws IOException;
	
	void addSalt(OutputStream outputStream) throws IOException;
	
}
