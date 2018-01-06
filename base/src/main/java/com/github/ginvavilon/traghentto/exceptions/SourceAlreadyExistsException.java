/**
 * 
 */
package com.github.ginvavilon.traghentto.exceptions;

/**
 * @author vbaraznovsky
 *
 */
public class SourceAlreadyExistsException extends SourceException {

    private static final long serialVersionUID = 6231314479607058772L;

    public SourceAlreadyExistsException() {
        super();
    }

    public SourceAlreadyExistsException(String pDetailMessage, Throwable pThrowable) {
        super(pDetailMessage, pThrowable);
    }

    public SourceAlreadyExistsException(String pDetailMessage) {
        super(pDetailMessage);
    }

    public SourceAlreadyExistsException(Throwable pThrowable) {
        super(pThrowable);
    }

}
