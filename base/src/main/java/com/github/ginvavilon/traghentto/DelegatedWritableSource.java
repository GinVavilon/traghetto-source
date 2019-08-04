/**
 * 
 */
package com.github.ginvavilon.traghentto;

import java.io.IOException;
import java.io.OutputStream;

import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.exceptions.RenameException;
import com.github.ginvavilon.traghentto.params.StreamParams;

/**
 * @author vbaraznovsky
 *
 */
public class DelegatedWritableSource<T extends WritableSource> extends DelegatedSource<T>
        implements WritableSource {

    protected DelegatedWritableSource(SourceProvider<T> sourceProvider) {
        super(sourceProvider);
    }

    protected DelegatedWritableSource(T source) {
        super(source);
    }

    @Override
    public boolean delete() {
        return getSource().delete();
    }

    @Override
    public boolean canBeDeleted() {
        return getSource().canBeDeleted();
    }


    @Override
    public RenamedSource createRenamedSource(String name) {
        return getSource().createRenamedSource(name);
    }

    @Override
    public boolean canBeRenamed(RenamedSource source) {
        return getSource().canBeRenamed(source);
    }

    @Override
    public void rename(RenamedSource source) throws RenameException {
        getSource().rename(source);
    }

    @Override
    public boolean create() throws IOException {
        return getSource().create();
    }

    @Override
    public boolean createConteiner() throws IOException {
        return getSource().createConteiner();
    }

    @Override
    public boolean isWritable() {
        return getSource().isWritable();
    }

    @Override
    public WritableSource getChild(String name) {
        return getSource().getChild(name);
    }

    @Override
    public StreamResource<OutputStream> openOutputResource(StreamParams pParams)
            throws IOException, IOSourceException {
        return getSource().openOutputResource(pParams);
    }

    @Override
    public StreamResource<OutputStream> openOutputResource() throws IOException, IOSourceException {
        return getSource().openOutputResource();
    }

}
