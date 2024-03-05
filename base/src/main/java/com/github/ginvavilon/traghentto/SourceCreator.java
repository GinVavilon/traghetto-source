/**
 * 
 */
package com.github.ginvavilon.traghentto;

/**
 * @author Vladimir Baraznovsky
 *
 */
public interface SourceCreator<T extends Source> {
    T create(String param);

}
