/**
 * 
 */
package com.github.ginvavilon.traghentto;

/**
 * @author vbaraznovsky
 *
 */
public interface SourceCreator<T extends ISource> {
    T create(String param);

}
