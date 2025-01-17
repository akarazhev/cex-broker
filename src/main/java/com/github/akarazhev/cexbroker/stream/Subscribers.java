package com.github.akarazhev.cexbroker.stream;

import com.github.akarazhev.cexbroker.bybit.stream.BybitSubscriber;

public final class Subscribers {

    private Subscribers() {
        throw new UnsupportedOperationException();
    }

    public static Subscriber ofBybit(final DataHandler handler) {
        return BybitSubscriber.create(handler);
    }
}
