/**
 *
 */
package com.github.ginvavilon.traghentto.params;

/**
 * @author Vladimir Baraznovsky
 *
 */
public interface StreamParams {
    Object getProperty(String pKey, Object pDefault);

    int getProperty(String pKey, int pDefault);

    float getProperty(String pKey, float pDefault);

    long getProperty(String pKey, long pDefault);

    boolean getProperty(String pKey, boolean pDefault);

    String getProperty(String pKey, String pDefault);

    StreamParams changeProperty(String pKey, Object pValue);

}
