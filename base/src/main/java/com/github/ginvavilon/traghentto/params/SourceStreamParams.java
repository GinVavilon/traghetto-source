/**
 *
 */
package com.github.ginvavilon.traghentto.params;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class SourceStreamParams implements StreamParams {

    private Map<String, Object> mParams=new  HashMap<String, Object>();

    @Override
    public Object getProperty(String pKey, Object pDefault) {
	return get(pKey, pDefault);
    }

    private Object get(String pKey, Object pDefault) {
	if (!mParams.containsKey(pKey)){
	    return pDefault;
	}
	return mParams.get(pKey);
    }

    @Override
    public int getProperty(String pKey, int pDefault) {
	return (Integer) get(pKey, pDefault);
    }

    @Override
    public float getProperty(String pKey, float pDefault) {
	return  (Float) get(pKey, pDefault);
    }

    @Override
    public long getProperty(String pKey, long pDefault) {
	return  (Long) get(pKey, pDefault);
    }

    @Override
    public boolean getProperty(String pKey, boolean pDefault) {
	return  (Boolean) get(pKey, pDefault);
    }

    @Override
    public String getProperty(String pKey, String pDefault) {
	return get(pKey, pDefault).toString();
    }

    public SourceStreamParams set(String pKey,Object pValue){
	mParams.put(pKey, pValue);
	return this;
    }

    @Override
    public StreamParams changeProperty(String pKey, Object pValue) {
	return set(pKey, pValue);
    }

}
