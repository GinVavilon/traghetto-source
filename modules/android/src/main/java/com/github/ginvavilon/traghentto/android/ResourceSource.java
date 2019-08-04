/**
 *
 */
package com.github.ginvavilon.traghentto.android;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.ginvavilon.traghentto.BaseSource;
import com.github.ginvavilon.traghentto.StreamUtils;
import com.github.ginvavilon.traghentto.UriConstants;
import com.github.ginvavilon.traghentto.android.creators.AndroidSourceCreator;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class ResourceSource extends BaseSource implements ParselFileDesriptorSource ,AssetFileDescriptorSource{

    private final Resources mResources;
    private final int mId;

    public ResourceSource(Resources pResources, int pId) {
	super();
	mResources = pResources;
	mId = pId;
    }

    @Override
    public List<ResourceSource> getChildren() {
        return new ArrayList<ResourceSource>();
    }

    @Override
    public ResourceSource getChild(String pName) {
	return null;
    }

    @Override
    public boolean isConteiner() {
	return false;
    }

    @Override
    protected InputStream openInputStream(StreamParams pParams) throws IOException {
	return mResources.openRawResource(mId);
    }

    @Override
    public String getPath() {
	return mResources.getResourceName(mId);
    }

    @Override
    public String getName() {
	return mResources.getResourceEntryName(mId);
    }

    public String getTypeName() {
	    return mResources.getResourceTypeName(mId);
    }

    public Uri getUri() {
	Uri.Builder builder = Uri.EMPTY.buildUpon();
	builder.scheme(UriConstants.RESOURCE_SCHEME);
	builder.authority(mResources.getResourcePackageName(mId));
	builder.path(mResources.getResourceTypeName(mId));
	builder.appendPath(mResources.getResourceEntryName(mId));
	return builder.build();
    }

    @Override
    public boolean exists() {
	return true;
    }

    @Override
    public long getLenght() {
	InputStream openInputStream=null;
	try {
	    openInputStream = openInputStream(null);
	    return openInputStream.available();
	} catch (IOException e) {
	} finally {
	    StreamUtils.close(openInputStream);
	}
	return UNKNOWN_LENGHT;
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
    public String toString() {
	return "@" + getPath();
    }

    @Override
    public String getUriString() {
        return getUri().toString();
    }

    @Override
    public ParcelFileDescriptor openParcelFileDescriptor() {
        return mResources.openRawResourceFd(mId).getParcelFileDescriptor();
    }

    @Override
    public AssetFileDescriptor openAssetFileDescriptor() throws IOException {
        return mResources.openRawResourceFd(mId);
    }

    public static final AndroidSourceCreator<ResourceSource> ANDROID_CREATOR = new AndroidSourceCreator<ResourceSource>() {

        @Override
        public ResourceSource create(Context pContext, Uri pUri) {
            Resources resources = pContext.getResources();
            String packageName = pUri.getAuthority();
            if (packageName.equals("")) {
                packageName = pContext.getPackageName();
            }
            List<String> segments = pUri.getPathSegments();
            String type;
            String name;

            if (segments.size() > 1) {
                type = segments.get(0);
                name = segments.get(1);
            } else {
                type = null;
                name = segments.get(0);
            }

            int id = resources.getIdentifier(name, type, packageName);
            return new ResourceSource(resources, id);
        }
    };

}
