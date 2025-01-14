package com.github.akarazhev.cexbroker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.akarazhev.cexbroker.client.ClientFactory;
import com.github.akarazhev.cexbroker.client.EventListener;
import io.reactivex.rxjava3.core.Observable;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ObservableFactory {

    public static Observable<String> createBybitObservable(final URI uri, final String[] topics) {
        return Observable.create(emitter -> {
            final Logger logger = LoggerFactory.getLogger(Application.class);
            final AtomicInteger reconnectAttempts = new AtomicInteger(0);
            final int maxReconnectAttempts = 10; // Increased for more persistence
            final long maxReconnectDelay = 30000; // Max delay of 30 seconds

            final EventListener listener = new EventListener() {
                private WebSocketClient client;

                @Override
                public void setClient(final WebSocketClient client) {
                    this.client = client;
                }

                @Override
                public void onOpen() {
                    reconnectAttempts.set(0); // Reset reconnect attempts on successful connection
                    subscribe(client, topics);
                }

                @Override
                public void onMessage(final String message) {
                    emitter.onNext(message);
                }

                @Override
                public void onClose() {
                    reconnect("Connection closed");
                }

                @Override
                public void onError(final Throwable error) {
                    reconnect("Error occurred: " + error.getMessage());
                }

                private void subscribe(final WebSocketClient client, final String[] topics) {
                    final Map<String, Object> request = new HashMap<>();
                    request.put("op", "subscribe");
                    request.put("args", topics);

                    final ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        client.send(objectMapper.writeValueAsString(request));
                    } catch (Exception e) {
                        logger.error("Failed to subscribe to topics", e);
                    }
                }

                private void reconnect(String reason) {
                    int attempts = reconnectAttempts.getAndIncrement();
                    if (attempts < maxReconnectAttempts) {
                        long delay = Math.min(1000 * (long) Math.pow(2, attempts), maxReconnectDelay);
                        logger.warn("{}. Attempting to reconnect in {} ms... (Attempt {})", reason, delay, attempts + 1);
                        emitter.onNext(reason + ". Reconnecting... (Attempt " + (attempts + 1) + ")");

                        Observable.timer(delay, TimeUnit.MILLISECONDS)
                                .subscribe(__ -> {
                                    try {
                                        client = ClientFactory.createWebSocketClient(uri, this);
                                        client.connect();
                                    } catch (Exception e) {
                                        reconnect("Failed to reconnect");
                                    }
                                });
                    } else {
                        logger.error("Max reconnection attempts reached. Closing observable.");
                        emitter.onComplete();
                    }
                }
            };

            final WebSocketClient client = ClientFactory.createWebSocketClient(uri, listener);
            listener.setClient(client);
            client.connect();
            emitter.setCancellable(client::close);
        });
    }
}
