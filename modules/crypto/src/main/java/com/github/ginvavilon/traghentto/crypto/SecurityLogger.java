package com.github.ginvavilon.traghentto.crypto;

class SecurityLogger {
    private final static boolean ENABLED = false;

    static void println(String string, Object message) {
        if (!ENABLED) {
            return;
        }
    	System.out.print(String.format("%12s - ", string));
    	System.out.print(message);
    	System.out.println();
    }

    static void println(String string, byte[] hash) {
        if (!ENABLED) {
            return;
        }
    	System.out.print(String.format("%12s - ", string));
        if (hash == null) {
            System.out.println("null");
            return;
        }
    	for (byte b : hash) {
    		System.out.print(String.format("%02X", b));
    		System.out.print(":");
    
    	}
    	System.out.println();
    
    }

}
