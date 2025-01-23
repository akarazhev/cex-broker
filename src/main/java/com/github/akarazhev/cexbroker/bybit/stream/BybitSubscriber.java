package com.github.akarazhev.cexbroker.bybit.stream;

import com.github.akarazhev.cexbroker.bybit.Constants;
import com.github.akarazhev.cexbroker.bybit.stream.data.KlineResponse;
import com.github.akarazhev.cexbroker.bybit.stream.data.Response;
import com.github.akarazhev.cexbroker.bybit.stream.data.TickerResponse;
import com.github.akarazhev.cexbroker.stream.Subscriber;
import com.github.akarazhev.cexbroker.stream.StreamHandler;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

public final class BybitSubscriber implements Subscriber {
    private static final class ResponseType {

        private ResponseType() {
            throw new UnsupportedOperationException();
        }

        private static final String TICKERS = "tickers";
        private static final String KLINE = "kline";
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(BybitSubscriber.class);
    private final Map<String, Response> responses;
    private final StreamHandler handler;

    private BybitSubscriber(final StreamHandler handler) {
        this.handler = handler;
        this.responses = Map.of(ResponseType.TICKERS, new TickerResponse(), ResponseType.KLINE, new KlineResponse());
    }

    public static BybitSubscriber create(final StreamHandler handler) {
        return new BybitSubscriber(handler);
    }

    @Override
    public Consumer<Map<String, Object>> onNext() {
        return response -> {
            final String topic = buildTopic(response);
            final String responseKey = getResponseKey(topic);
            if (responses.containsKey(responseKey)) {
                handler.handle(topic, responses.get(responseKey).process(response));
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

    private String buildTopic(final Map<String, Object> response) {
        return String.join(".", Constants.Topics.STREAM_TOPIC_PREFIX,
                response.get(Constants.Topics.TOPIC_FIELD).toString());
    }

    private String getResponseKey(final String topic) {
        return Arrays.stream(new String[]{ResponseType.TICKERS, ResponseType.KLINE})
                .filter(topic::contains)
                .findFirst()
                .orElse("");
    }
}
