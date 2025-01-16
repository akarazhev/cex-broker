package com.github.akarazhev.cexbroker.bybit.request;

import com.github.akarazhev.cexbroker.bybit.Constants;

public final class Requests {

    private Requests() {
    }

    public static Subscription ofSubscription(final String[] topics) {
        return new Subscription(Constants.Requests.SUBSCRIBE, topics);
    }

    public static Ping ofPing() {
        return new Ping(Constants.Requests.PING);
    }
}
