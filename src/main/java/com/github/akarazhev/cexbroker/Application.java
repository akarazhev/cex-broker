package com.github.akarazhev.cexbroker;

import com.github.akarazhev.cexbroker.stream.Config;
import com.github.akarazhev.cexbroker.stream.Filter;
import com.github.akarazhev.cexbroker.stream.Filters;
import com.github.akarazhev.cexbroker.stream.Mapper;
import com.github.akarazhev.cexbroker.stream.Mappers;
import com.github.akarazhev.cexbroker.stream.Observables;
import com.github.akarazhev.cexbroker.stream.StreamHandler;
import com.github.akarazhev.cexbroker.stream.Subscriber;
import com.github.akarazhev.cexbroker.stream.Subscribers;
import io.reactivex.rxjava3.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(final String[] args) {
        final long t = System.currentTimeMillis();
        LOGGER.info("Starting CEX Broker...");
        LOGGER.info(com.github.akarazhev.cexbroker.bybit.config.Config.print());
        LOGGER.info(Config.print());
//        final StreamHandler streamHandler = new StreamProducer();
        final StreamHandler consoleHandler = new StreamHandler() {
            @Override
            public void handle(final String topic, final Map<String, Object> data) {
                LOGGER.info("Topic: {}, Data: {}", topic, data);
            }

            @Override
            public void close() {
                LOGGER.info("Closing producer...");
            }
        };
        final Mapper bybitMapper = Mappers.ofBybit();
        final Filter bybitFilter = Filters.ofBybit();
        final Subscriber bybitSubscriber = Subscribers.ofBybit(consoleHandler);
        final Disposable bybitDisposable = Observables.ofBybit()
                .map(bybitMapper.map())
                .filter(bybitFilter.filter())
                .subscribe(bybitSubscriber.onNext(), bybitSubscriber.onError(), bybitSubscriber.onComplete());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down CEX Broker...");
            bybitDisposable.dispose();
        }));
        LOGGER.info("CEX Broker has been started in {} ms", System.currentTimeMillis() - t);
    }
}
