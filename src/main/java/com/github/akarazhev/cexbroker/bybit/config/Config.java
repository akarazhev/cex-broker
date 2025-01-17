package com.github.akarazhev.cexbroker.bybit.config;

import com.github.akarazhev.cexbroker.bybit.Constants;

import java.net.URI;
import java.util.Arrays;

public final class Config {
    private final static URI WEB_SOCKET_URI;
    private final static String[] TICKER_TOPICS;

    static {
        WEB_SOCKET_URI = URI.create(Constants.Config.WsPublicStream.SPOT_TEST_URL);
        final String value = System.getenv(Constants.Config.Keys.BYBIT_TICKER_TOPICS);
        TICKER_TOPICS = value != null && !value.isBlank() ?
                value.split(",") :
                Constants.Config.Defaults.BYBIT_TICKER_TOPICS;
    }

    private Config() {
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
