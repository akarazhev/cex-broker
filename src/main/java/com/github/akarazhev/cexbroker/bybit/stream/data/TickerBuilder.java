package com.github.akarazhev.cexbroker.bybit.stream.data;

import java.util.HashMap;
import java.util.Map;

import static com.github.akarazhev.cexbroker.bybit.stream.data.Constants.Field.CS;
import static com.github.akarazhev.cexbroker.bybit.stream.data.Constants.Field.TS;
import static com.github.akarazhev.cexbroker.bybit.stream.data.Constants.Field.TYPE;
import static com.github.akarazhev.cexbroker.util.TypeUtils.asDouble;

final class TickerBuilder {
    private static final class Ticker {

        private Ticker() {
            throw new UnsupportedOperationException();
        }

        private static final String SYMBOL = "symbol";

        private static final class Request {
            private Request() {
                throw new UnsupportedOperationException();
            }

            private static final String LAST_PRICE = "last_price";
            private static final String HIGH_PRICE_24H = "high_price_24h";
            private static final String LOW_PRICE_24H = "low_price_24h";
            private static final String PREV_PRICE_24H = "prev_price_24h";
            private static final String VOLUME_24H = "volume_24h";
            private static final String TURNOVER_24H = "turnover_24h";
            private static final String PRICE_24H_PCNT = "price_24h_pcnt";
            private static final String USD_INDEX_PRICE = "usd_index_price";
        }

        private static final class Response {

            private Response() {
                throw new UnsupportedOperationException();
            }

            private static final String LAST_PRICE = "lastPrice";
            private static final String HIGH_PRICE_24H = "highPrice24h";
            private static final String LOW_PRICE_24H = "lowPrice24h";
            private static final String PREV_PRICE_24H = "prevPrice24h";
            private static final String VOLUME_24H = "volume24h";
            private static final String TURNOVER_24H = "turnover24h";
            private static final String PRICE_24H_PCNT = "price24hPcnt";
            private static final String USD_INDEX_PRICE = "usdIndexPrice";
        }
    }

    private final Map<String, Object> ticker = new HashMap<>();

    public TickerBuilder setType(final String type) {
        ticker.put(TYPE, type);
        return this;
    }

    public TickerBuilder setTs(final long ts) {
        ticker.put(TS, ts);
        return this;
    }

    public TickerBuilder setCs(final int cs) {
        ticker.put(CS, cs);
        return this;
    }

    public TickerBuilder setData(final Map<String, Object> data) {
        if (data != null) {
            ticker.put(Ticker.SYMBOL, data.get(Ticker.SYMBOL));
            ticker.put(Ticker.Request.LAST_PRICE, asDouble(data.get(Ticker.Response.LAST_PRICE)));
            ticker.put(Ticker.Request.HIGH_PRICE_24H, asDouble(data.get(Ticker.Response.HIGH_PRICE_24H)));
            ticker.put(Ticker.Request.LOW_PRICE_24H, asDouble(data.get(Ticker.Response.LOW_PRICE_24H)));
            ticker.put(Ticker.Request.PREV_PRICE_24H, asDouble(data.get(Ticker.Response.PREV_PRICE_24H)));
            ticker.put(Ticker.Request.VOLUME_24H, asDouble(data.get(Ticker.Response.VOLUME_24H)));
            ticker.put(Ticker.Request.TURNOVER_24H, asDouble(data.get(Ticker.Response.TURNOVER_24H)));
            ticker.put(Ticker.Request.PRICE_24H_PCNT, asDouble(data.get(Ticker.Response.PRICE_24H_PCNT)));
            ticker.put(Ticker.Request.USD_INDEX_PRICE, asDouble(data.get(Ticker.Response.USD_INDEX_PRICE)));
        }

        return this;
    }

    public Map<String, Object> build() {
        return ticker;
    }
}
