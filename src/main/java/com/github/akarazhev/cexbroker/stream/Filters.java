package com.github.akarazhev.cexbroker.stream;

import com.github.akarazhev.cexbroker.bybit.stream.BybitFilter;

public final class Filters {

    private Filters() {
        throw new UnsupportedOperationException();
    }

    public static Filter ofBybit() {
        return BybitFilter.create();
    }
}
