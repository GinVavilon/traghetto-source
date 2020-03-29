/**
 * 
 */
package com.github.ginvavilon.traghentto.crypto;

import java.security.Key;

import javax.crypto.Cipher;

@FunctionalInterface
public interface KeyProvider {

    /**
     * @see Cipher#DECRYPT_MODE
     * @see Cipher#ENCRYPT_MODE
     */
    Key getKey(int mode);

}
