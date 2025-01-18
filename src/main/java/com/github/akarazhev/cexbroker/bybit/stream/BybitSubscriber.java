package com.github.akarazhev.cexbroker.bybit.stream;

import com.github.akarazhev.cexbroker.bybit.Constants;
import com.github.akarazhev.cexbroker.stream.Subscriber;
import com.github.akarazhev.cexbroker.stream.StreamHandler;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

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
        return data -> {
            final String topic = String.join(".", Constants.Topics.STREAM_TOPIC_PREFIX,
                    data.get(Constants.Topics.TOPIC_FIELD).toString());
            handler.handle(topic, data);
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
