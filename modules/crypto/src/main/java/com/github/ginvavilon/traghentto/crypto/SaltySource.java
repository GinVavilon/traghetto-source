/**
 * 
 */
package com.github.ginvavilon.traghentto.crypto;

import com.github.ginvavilon.traghentto.DelegatedSource;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.crypto.salt.Salt;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.params.StreamParams;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class SaltySource<T extends Source> extends DelegatedSource<T> implements Source {

    private final Salt mSalt;

    public SaltySource(T source, Salt salt) {
        super(source);
        mSalt = salt;
    }

    @Override
    public StreamResource<InputStream> openResource(StreamParams pParams)
            throws IOSourceException, IOException {
        return wrapStreamResource(getSource().openResource(pParams), mSalt::removeSalt);

    }

    @Override
    public List<? extends Source> getChildren() {
        List<Source> list = new ArrayList<>();
        List<? extends Source> children = getSource().getChildren();
        if (children == null) {
            return null;
        }
        for (Source source : children) {
            Source cryptoSource = wrapChild(source);
            list.add(cryptoSource);
        }
        return list;
    }

    protected SaltySource<? extends Source> wrapChild(Source source) {
        return new SaltySource<>(source, mSalt);
    }

    @Override
    public Source getChild(String name) {
        return wrapChild(getSource().getChild(name));
    }

}
