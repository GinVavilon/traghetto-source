/**
 *
 */
package com.github.ginvavilon.traghentto;

import java.io.IOException;
import java.io.OutputStream;

import com.github.ginvavilon.traghentto.params.ISourceStreamParams;

/**
 * @author Vladimir Baraznovsky
 *
 */
public interface IWritableSource extends ICanDeleteSource {
    OutputStream openOutputStream() throws IOException;
    boolean create() throws IOException;
    boolean createConteiner() throws IOException;
    IWritableSource getChild(String name);
    OutputStream openOutputStream(ISourceStreamParams pParams) throws IOException;
}
