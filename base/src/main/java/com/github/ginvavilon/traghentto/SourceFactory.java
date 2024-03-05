package com.github.ginvavilon.traghentto;

public interface SourceFactory<T extends Source>{

    T create(String uri);

}
