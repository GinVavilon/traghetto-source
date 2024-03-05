/**
 * 
 */
package com.github.ginvavilon.traghentto.crypto;

public class UnavailableCipherException extends Exception {

    public UnavailableCipherException() {
        super();
    }

    public UnavailableCipherException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public UnavailableCipherException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnavailableCipherException(String message) {
        super(message);
    }

    public UnavailableCipherException(Throwable cause) {
        super(cause);
    }


}
