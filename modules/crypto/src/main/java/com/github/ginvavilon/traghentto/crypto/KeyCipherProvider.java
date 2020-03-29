/**
 * 
 */
package com.github.ginvavilon.traghentto.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

/**
 * @author Vladimir Baraznovsky
 *
 */
public abstract class KeyCipherProvider implements CipherProvider {

    @Override
    public Cipher getCipher(int mode) throws UnavailableCipherException {

        try {
            Cipher cipher = Cipher.getInstance(getAlgorithm());
            Key key = getKey(mode);
            byte[] iv = getIv(cipher.getBlockSize());
            if (iv != null) {
                IvParameterSpec ivParams = new IvParameterSpec(iv);
                cipher.init(mode, key, ivParams);
            } else {
                cipher.init(mode, key);

            }
            SecurityLogger.println("cipher", cipher.getAlgorithm());
            SecurityLogger.println("cipher-key-format", key.getFormat());
            SecurityLogger.println("cipher-key", key.getEncoded());
            SecurityLogger.println("cipher-iv", cipher.getIV());

            return cipher;

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException e) {
            throw new UnavailableCipherException(e);
        }
    }

    protected abstract byte[] getIv(int blockSize);

    protected abstract Key getKey(int mode);

    protected abstract String getAlgorithm();

}
