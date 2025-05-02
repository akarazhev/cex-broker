package com.github.akarazhev.cexbroker;

import com.github.akarazhev.cexbroker.bybit.stream.BybitFilter;
import com.github.akarazhev.cexbroker.bybit.stream.BybitMapper;
import com.github.akarazhev.cexbroker.bybit.stream.BybitSubscriber;
//import com.github.akarazhev.cexbroker.kafka.StreamProducer;
import com.github.akarazhev.cexbroker.stream.StreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.github.akarazhev.cexbroker.bybit.BybitConfig.getPublicSubscribeTopics;
import static com.github.akarazhev.cexbroker.bybit.BybitConfig.getPublicTestnetSpot;

public final class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(final String[] args) {
        final var t = System.currentTimeMillis();
        LOGGER.info("Starting CEX Broker...");
//        final var bybitHandler = new StreamProducer();
        final var consoleHandler = new StreamHandler() {
            @Override
            public void handle(final String topic, final Map<String, Object> data) {
                LOGGER.info("Topic: {}, Data: {}", topic, data);
            }

            @Override
            public void close() {
                LOGGER.info("Closing producer...");
            }
        };
        final var bybitSubscriber = BybitSubscriber.create(consoleHandler);
        final var publicTestnetSpot = DataFlows.ofBybit(getPublicTestnetSpot(), getPublicSubscribeTopics())
                .map(BybitMapper.ofMap())
                .filter(BybitFilter.ofFilter())
                .subscribe(bybitSubscriber.onNext(), bybitSubscriber.onError(), bybitSubscriber.onComplete());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down CEX Broker...");
            publicTestnetSpot.dispose();
            consoleHandler.close();
        }));
        LOGGER.info("CEX Broker has been started in {} ms", System.currentTimeMillis() - t);
    }
}
