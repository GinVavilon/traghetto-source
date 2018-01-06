/**
 *
 */
package com.github.ginvavilon.traghentto.exceptions;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class SourceException extends Exception {

    private static final long serialVersionUID = 1L;

    public SourceException() {
        super();
    }

    public SourceException(String pDetailMessage, Throwable pThrowable) {
        super(pDetailMessage, pThrowable);
    }

    public SourceException(String pDetailMessage) {
        super(pDetailMessage);
    }

    public SourceException(Throwable pThrowable) {
        super(pThrowable);
    }

}
