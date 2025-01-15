package com.github.akarazhev.cexbroker.bybit.request;

public final class Requests {

    private Requests() {
    }

    public static Subscription ofSubscription(final String[] topics) {
        return new Subscription("subscribe", topics);
    }

    public static Ping ofPing() {
        return new Ping();
    }
}
