package com.github.akarazhev.cexbroker;

import com.github.akarazhev.cexbroker.config.Config;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);


    public static void main(String[] args) {
        long t = System.currentTimeMillis();
        Config config = Config.getConfig();
        LOGGER.info(config.toString());
//        KafkaProducer<String, String> producer = new KafkaProducer<>(config.getKafkaProperties());
//        MessageHandler messageHandler = new KafkaMessageHandler(producer, config.getKafkaTopic());
        Observable<String> observable = ObservableFactory.createBybitObservable(config.getWebSocketUrl(),
                config.getWebSocketTopics());
        Disposable disposable = observable.subscribe(
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
        LOGGER.info("Application started after {} ms", System.currentTimeMillis() - t);
    }
}
