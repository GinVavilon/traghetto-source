/**
 *
 */
package com.github.ginvavilon.traghentto;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class Logger {

    public interface LogHandler {

        void d(int pType, String pMessage, Object[] pArgs);

        void i(int pType, String pMessage, Object[] pArgs);

        void e(int pType, Throwable pE);

        void e(int pType, String pMessage, Object[] pArgs, Throwable pThrowable);

    }

    public static class Level {

        public static final int STREAM = 1;
        public static final int CACHE = 2;
        public static final int HTTP = 4;
        public static final int SOURCE = 8;
        public static final int MODULE = 16;
        public static final int APPLICATION = 32;

    }

    private static Set<LogHandler> sHandlers = new HashSet<>();

    public static boolean register(LogHandler pArg0) {
        return sHandlers.add(pArg0);
    }

    public static void d(int pType, String pMessage, Object... args) {
        for (LogHandler handler : sHandlers) {
            handler.d(pType, pMessage, args);
        }
    }

    public static void e(String pMessage, Throwable pE, Object... args) {
        for (LogHandler handler : sHandlers) {
            handler.e(0, pMessage, args, pE);
        }
    }

    public static void e(int pType, Throwable pE) {
        for (LogHandler handler : sHandlers) {
            handler.e(pType, pE);
        }
    }

    public static void e(Throwable pE) {
        for (LogHandler handler : sHandlers) {
            handler.e(0, pE);
        }
    }


}
