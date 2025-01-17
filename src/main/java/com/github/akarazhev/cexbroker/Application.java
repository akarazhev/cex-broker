package com.github.akarazhev.cexbroker;

import com.github.akarazhev.cexbroker.bybit.config.Config;
import com.github.akarazhev.cexbroker.stream.*;
import io.reactivex.rxjava3.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(final String[] args) {
        final long t = System.currentTimeMillis();
        LOGGER.info("Starting CEX Broker...");
        LOGGER.info(Config.print());
//        KafkaProducer<String, String> producer = new KafkaProducer<>(config.getKafkaProperties());
//        MessageHandler messageHandler = new KafkaMessageHandler(producer, config.getKafkaTopic());
//        DataHandler handler = object -> LOGGER.info("Received: {}", object);
        DataHandler handler = new DataHandler() {
            @Override
            public void handle(Map<String, Object> object) {
                LOGGER.info("Received: {}", object);
            }

            @Override
            public void close() {
                LOGGER.info("Closing producer...");
            }
        };
        final Mapper bybitMapper = Mappers.ofBybit();
        final Filter bybitFilter = Filters.ofBybit();
        final Subscriber bybitSubscriber = Subscribers.ofBybit(handler);
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
