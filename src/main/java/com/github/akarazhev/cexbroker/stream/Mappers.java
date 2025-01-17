package com.github.akarazhev.cexbroker.stream;

import com.github.akarazhev.cexbroker.bybit.stream.BybitMapper;

public final class Mappers {

    private Mappers() {
        throw new UnsupportedOperationException();
    }

    public static Mapper ofBybit() {
        return BybitMapper.create();
    }
}
