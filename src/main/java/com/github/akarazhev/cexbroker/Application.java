package com.github.akarazhev.cexbroker;

import com.github.akarazhev.cexbroker.bybit.config.Config;
import com.github.akarazhev.cexbroker.function.Filters;
import com.github.akarazhev.cexbroker.function.Mappers;
import com.github.akarazhev.cexbroker.pipline.Observables;
import io.reactivex.rxjava3.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(final String[] args) {
        long t = System.currentTimeMillis();
        final Config config = Config.getConfig();
        LOGGER.info(config.toString());
//        KafkaProducer<String, String> producer = new KafkaProducer<>(config.getKafkaProperties());
//        MessageHandler messageHandler = new KafkaMessageHandler(producer, config.getKafkaTopic());
        final Disposable bybitDisposable = Observables.ofBybit(config)
                .map(Mappers.ofBybit())
                .filter(Filters.ofBybit())
                .subscribe(
                message -> {
                    LOGGER.info("Received: {}", message);
//                    messageHandler.handleMessage(message);
                },
                throwable -> LOGGER.error("WebSocket error", throwable),
                () -> {
                    LOGGER.info("WebSocket closed");
//                    producer.close();
                }
        );
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down CEX Broker...");
            bybitDisposable.dispose();
        }));
        LOGGER.info("CEX Broker started after {} ms", System.currentTimeMillis() - t);
    }
}
