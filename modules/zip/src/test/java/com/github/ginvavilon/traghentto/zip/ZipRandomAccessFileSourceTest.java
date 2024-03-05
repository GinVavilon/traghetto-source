/**
 * 
 */
package com.github.ginvavilon.traghentto.zip;

/**
 * @author Vladimir Baraznovsky
 *
 */
public class ZipRandomAccessFileSourceTest extends BaseZipSourceTest<ZipRandomAccessFileSource> {

    @Override
    public ZipRandomAccessFileSource wrapSource(ZipFileSource zipFileSource) {
        return new ZipRandomAccessFileSource(zipFileSource);
    }

}
