package com.github.akarazhev.cexbroker.bybit;

import com.github.akarazhev.cexbroker.config.AppConfig;

import java.net.URI;
import java.util.Arrays;

public final class BybitConfig {
    private static final URI WEB_SOCKET_URI = URI.create(AppConfig.getAsString("bybit.public.testnet.spot"));
    private static final String[] SUBSCRIBE_TOPICS = AppConfig.getAsString("bybit.subscribe.topics").split(",");
    private static final long PING_INTERVAL = AppConfig.getAsLong("bybit.ping.interval");

    private BybitConfig() {
        throw new UnsupportedOperationException();
    }

    public static URI getWebSocketUri() {
        return WEB_SOCKET_URI;
    }

    public static String[] getSubscribeTopics() {
        return SUBSCRIBE_TOPICS;
    }

    public static long getPingInterval() {
        return PING_INTERVAL;
    }

    public static String print() {
        return "Bybit Config {" +
                "webSocketUri=" + getWebSocketUri() +
                ", subscribeTopics=" + Arrays.toString(getSubscribeTopics()) +
                ", pingInterval=" + getPingInterval() +
                '}';
    }
}
