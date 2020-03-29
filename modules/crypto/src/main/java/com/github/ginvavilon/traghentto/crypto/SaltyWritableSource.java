/**
 * 
 */
package com.github.ginvavilon.traghentto.crypto;

import com.github.ginvavilon.traghentto.DelegatedWritableSource;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.WritableSource;
import com.github.ginvavilon.traghentto.crypto.salt.Salt;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SaltyWritableSource<T extends WritableSource> extends DelegatedWritableSource<T> {

    private final Salt mSalt;

    protected SaltyWritableSource(T source, Salt salt) {
        super(source);
        mSalt = salt;
    }

    @Override
    public StreamResource<OutputStream> openOutputResource() throws IOException, IOSourceException {
        return wrapStreamResource(super.openOutputResource(), mSalt::addSalt);
    }

    @Override
    public StreamResource<OutputStream> openOutputResource(StreamParams pParams)
            throws IOException, IOSourceException {
        return wrapStreamResource(super.openOutputResource(pParams), mSalt::addSalt);
    }

    @Override
    public StreamResource<InputStream> openResource(StreamParams pParams)
            throws IOSourceException, IOException {
        return wrapStreamResource(super.openResource(pParams), mSalt::removeSalt);
    }

    @Override
    public List<? extends Source> getChildren() {
        List<Source> list = new ArrayList<>();
        List<? extends Source> children = getSource().getChildren();
        if (children == null) {
            return null;
        }
        for (Source source : children) {
            WritableSource cryptoSource = wrapChild(source);
            list.add(cryptoSource);
        }
        return list;
    }

    protected SaltyWritableSource<? extends WritableSource> wrapChild(Source source) {
        return new SaltyWritableSource<>((WritableSource) source, mSalt);
    }

    @Override
    public WritableSource getChild(String name) {
        return wrapChild(getSource().getChild(name));
    }

}
