package com.github.akarazhev.cexbroker.bybit.request;

import com.github.akarazhev.cexbroker.bybit.BybitConstants;
import com.github.akarazhev.cexbroker.util.JsonUtils;

public final class Request {

    private Request() {
        throw new UnsupportedOperationException();
    }

    public static String ofSubscription(final String[] topics) {
        return JsonUtils.objectToJson(new Subscription(BybitConstants.Request.SUBSCRIBE, topics));
    }

    public static String ofPing() {
        return JsonUtils.objectToJson(new Ping(BybitConstants.Request.PING));
    }
}
