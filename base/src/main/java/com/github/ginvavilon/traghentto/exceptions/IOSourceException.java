/**
 *
 */
package com.github.ginvavilon.traghentto.exceptions;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class IOSourceException extends SourceException {

    private static final long serialVersionUID = 1374056451752550933L;
    private String mData;

    public IOSourceException() {
        super();
    }

    public IOSourceException(String pDetailMessage, Throwable pThrowable) {
        super(pDetailMessage, pThrowable);
    }

    public IOSourceException(String pDetailMessage) {
        super(pDetailMessage);
    }

    public IOSourceException(Throwable pThrowable) {
        super(pThrowable);
    }

    public String getData() {
        return mData;
    }

    public void setData(String pData) {
        mData = pData;
    }

}
