/**
 * 
 */
package com.github.ginvavilon.traghentto.zip;

import com.github.ginvavilon.traghentto.RandomAccessSource;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class RandomAccessZipSourceTest extends BaseZipSourceTest<RandomAccessSource> {

    @Override
    public RandomAccessSource wrapSource(ZipFileSource zipFileSource) {
        return new RandomAccessSource(zipFileSource);
    }


}
