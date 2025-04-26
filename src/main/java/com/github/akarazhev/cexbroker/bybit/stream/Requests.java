package com.github.akarazhev.cexbroker.bybit.stream;

import com.github.akarazhev.cexbroker.util.JsonUtils;

final class Requests {
    private static final String SUBSCRIBE = "subscribe";
    private static final String PING = "ping";

    private Requests() {
        throw new UnsupportedOperationException();
    }

    public static String ofSubscription(final String[] topics) {
        return JsonUtils.objectToJson(new Subscription(SUBSCRIBE, topics));
    }

    public static String ofPing() {
        return JsonUtils.objectToJson(new Ping(PING));
    }
}
