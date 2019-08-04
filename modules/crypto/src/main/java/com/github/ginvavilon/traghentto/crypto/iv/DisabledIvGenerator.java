package com.github.ginvavilon.traghentto.crypto.iv;

public class DisabledIvGenerator implements IvGenerator {

    @Override
    public byte[] generateIv(int size) {
        return null;
    }

}
