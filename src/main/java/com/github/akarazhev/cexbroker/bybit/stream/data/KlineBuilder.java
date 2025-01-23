package com.github.akarazhev.cexbroker.bybit.stream.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.akarazhev.cexbroker.bybit.stream.data.Constants.Field.TS;
import static com.github.akarazhev.cexbroker.bybit.stream.data.Constants.Field.TYPE;
import static com.github.akarazhev.cexbroker.util.TypeUtils.asBoolean;
import static com.github.akarazhev.cexbroker.util.TypeUtils.asDouble;
import static com.github.akarazhev.cexbroker.util.TypeUtils.asInteger;
import static com.github.akarazhev.cexbroker.util.TypeUtils.asLong;

final class KlineBuilder {
    private static final class Kline {

        private Kline() {
            throw new UnsupportedOperationException();
        }

        private static final String START = "start";
        private static final String END = "end";
        private static final String INTERVAL = "interval";
        private static final String OPEN = "open";
        private static final String CLOSE = "close";
        private static final String HIGH = "high";
        private static final String LOW = "low";
        private static final String VOLUME = "volume";
        private static final String TURNOVER = "turnover";
        private static final String CONFIRM = "confirm";
        private static final String TIMESTAMP = "timestamp";
    }

    private final Map<String, Object> kline = new HashMap<>();

    public KlineBuilder setType(final String type) {
        kline.put(TYPE, type);
        return this;
    }

    public KlineBuilder setTs(final long ts) {
        kline.put(TS, ts);
        return this;
    }

    public KlineBuilder setData(final List<Map<String, Object>> klineData) {
        if (klineData != null && !klineData.isEmpty()) {
            final Map<String, Object> data = klineData.get(0);
            kline.put(Kline.START, asLong(data.get(Kline.START)));
            kline.put(Kline.END, asLong(data.get(Kline.END)));
            kline.put(Kline.INTERVAL, asInteger(data.get(Kline.INTERVAL)));
            kline.put(Kline.OPEN, asDouble(data.get(Kline.OPEN)));
            kline.put(Kline.CLOSE, asDouble(data.get(Kline.CLOSE)));
            kline.put(Kline.HIGH, asDouble(data.get(Kline.HIGH)));
            kline.put(Kline.LOW, asDouble(data.get(Kline.LOW)));
            kline.put(Kline.VOLUME, asDouble(data.get(Kline.VOLUME)));
            kline.put(Kline.TURNOVER, asDouble(data.get(Kline.TURNOVER)));
            kline.put(Kline.CONFIRM, asBoolean(data.get(Kline.CONFIRM)));
            kline.put(Kline.TIMESTAMP, asLong(data.get(Kline.TIMESTAMP)));
        }

        return this;
    }

    public Map<String, Object> build() {
        return kline;
    }
}
