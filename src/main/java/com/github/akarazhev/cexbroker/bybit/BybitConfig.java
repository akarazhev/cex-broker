package com.github.akarazhev.cexbroker.bybit;

import com.github.akarazhev.cexbroker.config.AppConfig;

import java.net.URI;
import java.util.Arrays;

public final class BybitConfig {
    private final static URI WEB_SOCKET_URI = URI.create(AppConfig.getAsString("bybit.public.testnet.spot"));
    private final static String[] TICKER_TOPICS = AppConfig.getAsString("bybit.public.ticker.topics").split(",");

    private BybitConfig() {
        throw new UnsupportedOperationException();
    }

    public static URI getWebSocketUri() {
        return WEB_SOCKET_URI;
    }

    public static String[] getTickerTopics() {
        return TICKER_TOPICS;
    }

    public static String print() {
        return "Bybit Config {" +
                "webSocketUri=" + WEB_SOCKET_URI +
                ", tickerTopics=" + Arrays.toString(TICKER_TOPICS) +
                '}';
    }
}
