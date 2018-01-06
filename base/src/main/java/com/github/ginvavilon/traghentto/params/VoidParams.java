/**
 *
 */
package com.github.ginvavilon.traghentto.params;


/**
 * @author Vladimir Baraznovsky
 *
 */
public class VoidParams implements ISourceStreamParams {

    @Override
    public Object getProperty(String pKey, Object pDefault) {
	return pDefault;
    }

    @Override
    public int getProperty(String pKey, int pDefault) {
	return pDefault;
    }

    @Override
    public float getProperty(String pKey, float pDefault) {
	return pDefault;
    }

    @Override
    public String getProperty(String pKey, String pDefault) {
	return pDefault;
    }

    @Override
    public long getProperty(String pKey, long pDefault) {
	return pDefault;
    }

    @Override
    public boolean getProperty(String pKey, boolean pDefault) {
	return pDefault;
    }

    @Override
    public ISourceStreamParams changeProperty(String pKey, Object pDefault) {
	return this;
    }

}
