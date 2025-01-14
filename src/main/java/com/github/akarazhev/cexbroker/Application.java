package com.github.akarazhev.cexbroker;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.java_websocket.client.WebSocketClient;

import java.util.HashMap;
import java.util.Map;

public class Application {

    public static void main(String[] args) {
        Config config = Config.getConfig();
        KafkaProducer<String, String> producer = new KafkaProducer<>(config.getKafkaProperties());
        MessageHandler messageHandler = new KafkaMessageHandler(producer, config.getKafkaTopic());

        Observable<String> webSocketObservable = Observable.create(emitter -> {
            EventListener listener = new EventListener() {
                private WebSocketClient client;

                @Override
                public void setClient(final WebSocketClient client) {
                    this.client = client;
                }

                @Override
                public void onOpen() {
                    System.out.println("WebSocket opened");
                    subscribe(client, config.getWebSocketTopics());
                }

                @Override
                public void onMessage(final String message) {
                    emitter.onNext(message);
                }

                @Override
                public void onClose() {
                    System.out.println("WebSocket closed");
                    emitter.onComplete();
                }

                @Override
                public void onError(final Throwable error) {
                    System.err.println("Error: " + error);
                    emitter.onError(error);
                }
            };

            WebSocketClient client = WebSocketClientFactory.createWebSocketClient(config.getWebSocketUrl(), listener);
            listener.setClient(client);
            client.connectBlocking();
            emitter.setCancellable(client::close);
        });

        Disposable disposable = webSocketObservable.subscribe(
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
    }

    private static void subscribe(final WebSocketClient client, final String[] topics) {
        final Map<String, Object> request = new HashMap<>();
        request.put("op", "subscribe");
        request.put("args", topics);

        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            client.send(objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
