/**
 * 
 */
package com.github.ginvavilon.traghentto.exceptions;

/**
 * @author vbaraznovsky
 *
 */
public class RenameException extends SourceException {

    public RenameException() {
    }

    public RenameException(String pDetailMessage, Throwable pThrowable) {
        super(pDetailMessage, pThrowable);
    }

    public RenameException(String pDetailMessage) {
        super(pDetailMessage);
    }

    public RenameException(Throwable pThrowable) {
        super(pThrowable);
    }

}
