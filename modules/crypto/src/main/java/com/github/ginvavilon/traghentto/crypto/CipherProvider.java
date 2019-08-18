/**
 * 
 */
package com.github.ginvavilon.traghentto.crypto;

import javax.crypto.Cipher;

@FunctionalInterface
public interface CipherProvider {

    /**
     * @see Cipher#DECRYPT_MODE
     * @see Cipher#ENCRYPT_MODE
     */
    Cipher getCipher(int mode) throws UnavailableCipherException;

}
