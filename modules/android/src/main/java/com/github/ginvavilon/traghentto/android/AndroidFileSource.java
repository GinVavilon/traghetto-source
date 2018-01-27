/**
 *
 */
package com.github.ginvavilon.traghentto.android;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.ginvavilon.traghentto.android.creators.AndroidSourceCreator;
import com.github.ginvavilon.traghentto.file.FileSource;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class AndroidFileSource extends FileSource implements AndroidWritableSource {


    public AndroidFileSource(File pFile) {
        super(pFile);
    }

    @Override
    public List<? extends AndroidFileSource> getChildren() {
        List<? extends FileSource> children = super.getChildren();
        List<AndroidFileSource> list = new ArrayList<AndroidFileSource>();
        for (FileSource child : children) {
            list.add(new AndroidFileSource(child.getFile()));
        }
        return list;
    }

    @Override
    public AndroidFileSource getChild(String pName) {
        return new AndroidFileSource(super.getChild(pName).getFile());
    }

    @Override
    public Uri getUri() {
        return Uri.fromFile(getFile());
    }

    @Override
    public ParcelFileDescriptor openParcelFileDescriptor() throws IOException {
        return ParcelFileDescriptor.open(getFile(), ParcelFileDescriptor.MODE_READ_ONLY);
    }

    public static final AndroidSourceCreator<AndroidFileSource> ANDROID_CREATOR = new AndroidSourceCreator<AndroidFileSource>() {

        @Override
        public AndroidFileSource create(Context pContext, Uri pUri) {
            return new AndroidFileSource(new File(pUri.getPath()));
        }
    };

}
