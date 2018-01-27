/**
 *
 */
package com.github.ginvavilon.traghentto.android;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.ginvavilon.traghentto.Logger;
import com.github.ginvavilon.traghentto.StreamUtils;
import com.github.ginvavilon.traghentto.UriConstants;
import com.github.ginvavilon.traghentto.android.creators.AndroidSourceCreator;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class AssetSource implements AndroidSource {

    private final AssetManager mAssetManager;
    private final String mPath;

    public AssetSource(AssetManager pAssetManager, String pPath) {
	super();
	mAssetManager = pAssetManager;
	mPath = pPath;
    }

    @Override
    public List<AssetSource> getChildren() {
	List<AssetSource> list = new ArrayList<AssetSource>();

	try {
	    String[] listAssets = mAssetManager.list(mPath);
	    for (String child : listAssets) {
		list.add(getChild(child));
	    }
	} catch (IOException e) {
	    Logger.e(e);
	}
	return list;
    }

    @Override
    public AssetSource getChild(String pName) {
	int start = 0;
	int end = pName.length();
	if (pName.startsWith("/")){
	    start++;
	}
	if (pName.endsWith("/")){
	    end--;
	}
	String substring = pName.substring(start, end);
	return new AssetSource(mAssetManager, mPath+"/"+substring);
    }

    @Override
    public boolean isConteiner() {
	try {
	    InputStream open = mAssetManager.open(mPath);
	    StreamUtils.close(open);
	    return false;
	} catch (IOException e) {
	 //   Logger.e(e);
	}
	return true;
    }

    @Override
    public InputStream openInputStream(StreamParams pParams) throws IOException {
	return mAssetManager.open(mPath);
    }

    @Override
    public void closeStream(Closeable pStream) throws IOException {
	pStream.close();
    }

    @Override
    public String getPath() {
	return mPath;
    }

    @Override
    public String getName() {
	String name = mPath;
	int last = name.length()-1;
	if ('/'==(name.charAt(last))){
	    last--;
	}
	int index = name.lastIndexOf("/",last);
	return name.substring(index+1,last+1);
    }

    @Override
    public boolean exists() {
	String path = mPath;
	int last = path.length()-1;
	if ('/'==(path.charAt(last))){
	    last--;
	}
	int index = path.lastIndexOf("/",last);
	if(index<0){
	    index=0;
	}
	String parent = path.substring(0,index);
	String name=getName();
	try {
	    String[] list = mAssetManager.list(parent);
	    for (String string : list) {
		if(name.equals(string)){
		    return true;
		}
	    }
	} catch (IOException e) {

	}
	    return false;
    }

    @Override
    public Uri getUri() {
	Uri.Builder builder=Uri.EMPTY.buildUpon();
	builder.scheme(UriConstants.ASSET_SCHEME);
	builder.authority(UriConstants.EMPTY_AUTHOTITY);
	builder.path(mPath);
	return builder.build();
    }

    @Override
    public long getLenght() {
	try {
            AssetFileDescriptor fd = openAssetFileDescription();
	    if (fd != null) {
		return fd.getLength();
	    }
	} catch (IOException e) {
	}
	return UNKNOWN_LENGHT;
    }

    @Override
    public String toString() {
        return getUriString();
    }

    @Override
    public boolean isLocal() {
	return true;
    }

    @Override
    public boolean isDataAvailable() {
	return true;
    }

    @Override
    public String getUriString() {
        return getUri().toString();
    }

    @Override
    public ParcelFileDescriptor openParcelFileDescriptor() throws IOException {
        return openAssetFileDescription().getParcelFileDescriptor();
    }

    public AssetFileDescriptor openAssetFileDescription() throws IOException {
        return mAssetManager.openFd(mPath);
    }

    public static final AndroidSourceCreator<AssetSource> ANDROID_CREATOR = new AndroidSourceCreator<AssetSource>() {

        @Override
        public AssetSource create(Context pContext, Uri pUri) {
            String path = pUri.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return new AssetSource(pContext.getAssets(), path);
        }
    };

}
