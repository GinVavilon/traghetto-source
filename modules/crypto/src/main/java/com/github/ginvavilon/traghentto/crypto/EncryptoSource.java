package com.github.ginvavilon.traghentto.crypto;

import java.io.IOException;
import java.io.OutputStream;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import com.github.ginvavilon.traghentto.RenamedSource;
import com.github.ginvavilon.traghentto.Source;
import com.github.ginvavilon.traghentto.StreamResource;
import com.github.ginvavilon.traghentto.WritableSource;
import com.github.ginvavilon.traghentto.crypto.salt.Salt;
import com.github.ginvavilon.traghentto.exceptions.IOSourceException;
import com.github.ginvavilon.traghentto.exceptions.RenameException;
import com.github.ginvavilon.traghentto.params.StreamParams;
import com.github.ginvavilon.traghentto.params.VoidParams;

public class EncryptoSource<T extends WritableSource> extends CryptoSource<T> implements WritableSource {

	public EncryptoSource(T source, Key key) {
		super(source, key);
	}
	
	public EncryptoSource(T source, CryptoConfiguration configuration) {
		super(source, configuration);
	}


	@Override
	public boolean delete() {
		return getSource().delete();
	}

	@Override
	protected EncryptoSource<WritableSource> wrapChild(Source source) {
		return new EncryptoSource<WritableSource>((WritableSource) source, mConfiguration);
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
	public EncryptoSource<WritableSource> getChild(String name) {
		return wrapChild(getSource().getChild(name));
	}

	@Override
	public StreamResource<OutputStream> openOutputResource(StreamParams pParams) throws IOException, IOSourceException {
		StreamResource<OutputStream> resource = getSource().openOutputResource(pParams);
		return wrapStreamResource(resource, Cipher.ENCRYPT_MODE,new StreamWrapper<OutputStream>() {

			@Override
			public OutputStream wrap(OutputStream stream, Cipher cipher) {
				return new CipherOutputStream(stream, cipher);
			}
		}, Salt::addSalt);
	}

	@Override
	public StreamResource<OutputStream> openOutputResource() throws IOException, IOSourceException {
		return openOutputResource(new VoidParams());
	}

    private static RenamedSource extaractRenamedSource(RenamedSource source) {
        RenamedSource renamedSource = source;
        if (source instanceof EncryptoSource<?>) {
            EncryptoSource<?> encryptoSource = (EncryptoSource<?>) source;
            renamedSource = encryptoSource.getSource();
        }
        return renamedSource;
    }

    @Override
    public boolean canBeRenamed(RenamedSource source) {
        RenamedSource renamedSource = extaractRenamedSource(source);
        return getSource().canBeRenamed(renamedSource);

    }

    @Override
    public void rename(RenamedSource source) throws RenameException {
        RenamedSource renamedSource = extaractRenamedSource(source);
        getSource().rename(renamedSource);
    }

    @Override
    public boolean canBeDeleted() {
        return getSource().canBeDeleted();
    }

    @Override
    public boolean isWritable() {
        return getSource().isWritable();
    }

    @Override
    public RenamedSource createRenamedSource(String name) {
        return wrapChild(getSource().createRenamedSource(name));
    }
	
    @Override
    public String toString() {
        return "Encrypted!" + getSource();
    }

}
