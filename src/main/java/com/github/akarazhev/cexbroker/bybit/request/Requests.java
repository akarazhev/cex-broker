package com.github.akarazhev.cexbroker.bybit.request;

public final class Requests {

    public static Subscription subscription(final String[] topics) {
        return new Subscription("subscribe", topics);
    }

    public static Ping ping() {
        return new Ping();
    }
}
