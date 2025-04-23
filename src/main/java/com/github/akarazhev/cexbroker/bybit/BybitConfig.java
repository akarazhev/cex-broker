package com.github.akarazhev.cexbroker.bybit;

import com.github.akarazhev.cexbroker.config.AppConfig;

import java.net.URI;
import java.util.Arrays;

public final class BybitConfig {
    private final static URI WEB_SOCKET_URI =
            URI.create(AppConfig.getAsString(BybitConstants.WebSocket.PUBLIC_TESTNET_SPOT));
    private final static String[] TICKER_TOPICS =
            AppConfig.getAsString(BybitConstants.WebSocket.PUBLIC_TICKER_TOPICS).split(",");

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
