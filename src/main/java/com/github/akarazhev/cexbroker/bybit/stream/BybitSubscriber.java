package com.github.akarazhev.cexbroker.bybit.stream;

import com.github.akarazhev.cexbroker.bybit.Constants;
import com.github.akarazhev.cexbroker.stream.Subscriber;
import com.github.akarazhev.cexbroker.stream.StreamHandler;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.akarazhev.cexbroker.util.TypeUtils.asBoolean;
import static com.github.akarazhev.cexbroker.util.TypeUtils.asDouble;
import static com.github.akarazhev.cexbroker.util.TypeUtils.asInteger;
import static com.github.akarazhev.cexbroker.util.TypeUtils.asLong;

public final class BybitSubscriber implements Subscriber {
    private static final Logger LOGGER = LoggerFactory.getLogger(BybitSubscriber.class);
    private final StreamHandler handler;

    private BybitSubscriber(final StreamHandler handler) {
        this.handler = handler;
    }

    public static BybitSubscriber create(final StreamHandler handler) {
        return new BybitSubscriber(handler);
    }

    @Override
    public Consumer<Map<String, Object>> onNext() {
        return response -> {
            final String topic = String.join(".", Constants.Topics.STREAM_TOPIC_PREFIX,
                    response.get(Constants.Topics.TOPIC_FIELD).toString());
            if (topic.contains("tickers")) {
                final Map<String, Object> ticker = new HashMap<>();
                ticker.put("type", "spot");
                ticker.put("ts", asLong(response.get("ts")));
                ticker.put("cs", asInteger(response.get("cs")));

                final Map<String, Object> data = (Map<String, Object>) response.get("data");
                if (data != null) {
                    ticker.put("symbol", data.get("symbol"));
                    ticker.put("last_price", asDouble(data.get("lastPrice")));
                    ticker.put("high_price_24h", asDouble(data.get("highPrice24h")));
                    ticker.put("low_price_24h", asDouble(data.get("lowPrice24h")));
                    ticker.put("prev_price_24h", asDouble(data.get("prevPrice24h")));
                    ticker.put("volume_24h", asDouble(data.get("volume24h")));
                    ticker.put("turnover_24h", asDouble(data.get("turnover24h")));
                    ticker.put("price_24h_pcnt", asDouble(data.get("price24hPcnt")));
                    ticker.put("usd_index_price", asDouble(data.get("usdIndexPrice")));
                }

                handler.handle(topic, ticker);
            } else if (topic.contains("kline")) {
                final Map<String, Object> kline = new HashMap<>();
                kline.put("type", "spot");
                kline.put("ts", asLong(response.get("ts")));

                final List<Map<String, Object>> klineData = (List<Map<String, Object>>) response.get("data");
                for (final Map<String, Object> data : klineData) {
                    if (data != null) {
                        kline.put("start", asLong(data.get("start")));
                        kline.put("end", asLong(data.get("end")));
                        kline.put("interval", asInteger(data.get("interval")));
                        kline.put("open", asDouble(data.get("open")));
                        kline.put("close", asDouble(data.get("close")));
                        kline.put("high", asDouble(data.get("high")));
                        kline.put("low", asDouble(data.get("low")));
                        kline.put("volume", asDouble(data.get("volume")));
                        kline.put("turnover", asDouble(data.get("turnover")));
                        kline.put("confirm", asBoolean(data.get("confirm")));
                        kline.put("timestamp", asLong(data.get("timestamp")));
                    }
                }

                handler.handle(topic, kline);
            }
        };
    }

    @Override
    public Consumer<Throwable> onError() {
        return t -> LOGGER.error("WebSocket error", t);
    }

    @Override
    public Action onComplete() {
        return () -> {
            handler.close();
            LOGGER.info("WebSocket closed");
        };
    }
}
