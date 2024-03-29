/**
 * 
 */
package com.github.ginvavilon.traghentto.android;

import android.util.Log;

import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.Logger.LogHandler;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class AndroidLogHandler implements LogHandler {

    private static final String DEFAULT_TAG = "Traghentto";
    private final String mTag;

    public AndroidLogHandler(String tag) {
        super();
        mTag = tag;
    }

    @Override
    public void d(int pType, String pMessage, Object[] pArgs) {
        Log.d(mTag, String.format(pMessage, pArgs));
    }

    @Override
    public void i(int pType, String pMessage, Object[] pArgs) {
        Log.i(mTag, String.format(pMessage, pArgs));
    }

    @Override
    public void e(int pType, Throwable pE) {
        Log.e(mTag, pE.getMessage(), pE);
    }

    @Override
    public void e(int pType, String pMessage, Object[] pArgs, Throwable pThrowable) {
        Log.e(mTag, String.format(pMessage, pArgs), pThrowable);
    }

    public static void init(String tag) {
        Logger.register(new AndroidLogHandler(tag));
    }

    private static AtomicBoolean sInit = new AtomicBoolean(false);

    public static void init() {
        if (!sInit.getAndSet(true)) {
            init(DEFAULT_TAG);
        }
    }

}
