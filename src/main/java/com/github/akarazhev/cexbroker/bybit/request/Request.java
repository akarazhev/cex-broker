package com.github.akarazhev.cexbroker.bybit.request;

import com.github.akarazhev.cexbroker.util.JsonUtils;

public final class Request {
    private final static String SUBSCRIBE = "subscribe";
    private final static String PING = "ping";

    private Request() {
        throw new UnsupportedOperationException();
    }

    public static String ofSubscription(final String[] topics) {
        return JsonUtils.objectToJson(new Subscription(SUBSCRIBE, topics));
    }

    public static String ofPing() {
        return JsonUtils.objectToJson(new Ping(PING));
    }
}
