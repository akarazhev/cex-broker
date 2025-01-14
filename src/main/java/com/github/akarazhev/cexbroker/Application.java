package com.github.akarazhev.cexbroker;

import com.github.akarazhev.cexbroker.config.Config;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

public class Application {

    public static void main(String[] args) {
        long t = System.currentTimeMillis();
        Config config = Config.getConfig();
//        KafkaProducer<String, String> producer = new KafkaProducer<>(config.getKafkaProperties());
//        MessageHandler messageHandler = new KafkaMessageHandler(producer, config.getKafkaTopic());
        Observable<String> observable = ObservableFactory.createBybitObservable(config.getWebSocketUrl(),
                config.getWebSocketTopics());
        Disposable disposable = observable.subscribe(
                message -> {
                    System.out.println("Received: " + message);
//                    messageHandler.handleMessage(message);
                },
                throwable -> System.err.println("Error: " + throwable),
                () -> {
                    System.out.println("WebSocket closed");
//                    producer.close();
                }
        );
        System.out.println("Application startup took " + (System.currentTimeMillis() - t) + " ms");
    }
}
