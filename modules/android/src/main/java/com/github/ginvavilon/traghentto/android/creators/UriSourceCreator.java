package com.github.ginvavilon.traghentto.android.creators;

import android.net.Uri;

import com.github.ginvavilon.traghentto.Source;

public interface UriSourceCreator<T extends Source>  {
    T create(Uri uri);
}
