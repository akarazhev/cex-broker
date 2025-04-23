package com.github.akarazhev.cexbroker.bybit;

import com.github.akarazhev.cexbroker.config.AppConfig;

import java.net.URI;
import java.util.Arrays;

public final class BybitConfig {
    private static final URI WEB_SOCKET_URI = URI.create(AppConfig.getAsString("bybit.public.testnet.spot"));
    private static final String[] TICKER_TOPICS = AppConfig.getAsString("bybit.public.ticker.topics").split(",");
    private static final int MAX_RECONNECT_ATTEMPTS = AppConfig.getAsInt("bybit.max.reconnect.attempts");
    private static final long MAX_RECONNECT_DELAY = AppConfig.getAsLong("bybit.max.reconnect.delay");
    private static final long PING_INTERVAL = AppConfig.getAsLong("bybit.ping.interval");

    private BybitConfig() {
        throw new UnsupportedOperationException();
    }

    public static URI getWebSocketUri() {
        return WEB_SOCKET_URI;
    }

    public static String[] getTickerTopics() {
        return TICKER_TOPICS;
    }

    public static int getMaxReconnectAttempts() {
        return MAX_RECONNECT_ATTEMPTS;
    }

    public static long getMaxReconnectDelay() {
        return MAX_RECONNECT_DELAY;
    }

    public static long getPingInterval() {
        return PING_INTERVAL;
    }

    public static String print() {
        return "Bybit Config {" +
                "webSocketUri=" + getWebSocketUri() +
                ", tickerTopics=" + Arrays.toString(getTickerTopics()) +
                ", maxReconnectAttempts=" + getMaxReconnectAttempts() +
                ", maxReconnectDelay=" + getMaxReconnectDelay() +
                ", pingInterval=" + getPingInterval() +
                '}';
    }
}
