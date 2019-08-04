/**
 * 
 */
package com.github.ginvavilon.traghentto;

/**
 * @author vbaraznovsky
 *
 */
public interface SourceCreator<T extends Source> {
    T create(String param);

}
