/**
 * 
 */
package com.github.ginvavilon.traghentto;

import com.github.ginvavilon.traghentto.exceptions.RenameException;

/**
 * @author Vladimir Baraznovsky
 *
 */
public interface RenamedSource extends Source {

    RenamedSource createRenamedSource(String name);

    boolean canBeRenamed(RenamedSource source);

    void rename(RenamedSource source) throws RenameException;

}
