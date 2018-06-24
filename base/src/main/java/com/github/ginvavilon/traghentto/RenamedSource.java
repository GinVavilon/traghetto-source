/**
 * 
 */
package com.github.ginvavilon.traghentto;

import com.github.ginvavilon.traghentto.exceptions.RenameException;

/**
 * @author vbaraznovsky
 *
 */
public interface RenamedSource extends Source {

    boolean canBeRenamed(RenamedSource source);
    void rename(RenamedSource source) throws RenameException;

}
