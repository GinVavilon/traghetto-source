/**
 *
 */
package com.github.ginvavilon.traghentto;

/**
 * @author Vladimir Baraznovsky
 *
 */
public interface DeletableSource extends Source {

    boolean delete();

    boolean canBeDeleted();

}
