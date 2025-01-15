package com.github.akarazhev.cexbroker.bybit.config;

import java.net.URI;
import java.util.Arrays;

public final class Config {
    private final URI webSocketUri;
    private final String[] tickerTopics;

    private Config() {
        this.webSocketUri = URI.create(Constants.WsPublicStream.SPOT_TEST_URL);
        String value = System.getenv(Constants.Keys.BYBIT_TICKER_TOPICS);
        this.tickerTopics = value != null && !value.isBlank() ?
                value.split(",") :
                Constants.Defaults.BYBIT_TICKER_TOPICS;
    }

    public static Config getConfig() {
        return new Config();
    }

    public URI getWebSocketUri() {
        return webSocketUri;
    }

    public String[] getTickerTopics() {
        return tickerTopics;
    }

    @Override
    public String toString() {
        return "Bybit Config {" +
                "webSocketUri=" + webSocketUri +
                ", tickerTopics=" + Arrays.toString(tickerTopics) +
                '}';
    }
}
