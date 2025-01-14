package com.github.akarazhev.cexbroker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.akarazhev.cexbroker.client.ClientFactory;
import com.github.akarazhev.cexbroker.client.EventListener;
import io.reactivex.rxjava3.core.Observable;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public final class ObservableFactory {

    public static Observable<String> createBybitObservable(final URI uri, final String[] topics) {
        return Observable.create(emitter -> {
            final EventListener listener = new EventListener() {
                private WebSocketClient client;

                @Override
                public void setClient(final WebSocketClient client) {
                    this.client = client;
                }

                @Override
                public void onOpen() {
                    subscribe(client, topics);
                }

                @Override
                public void onMessage(final String message) {
                    emitter.onNext(message);
                }

                @Override
                public void onClose() {
                    emitter.onComplete();
                }

                @Override
                public void onError(final Throwable error) {
                    System.err.println("Error: " + error);
                    emitter.onError(error);
                }
            };

            final WebSocketClient client = ClientFactory.createWebSocketClient(uri, listener);
            listener.setClient(client);
            client.connect();
            emitter.setCancellable(client::close);
        });
    }

    private static void subscribe(final WebSocketClient client, final String[] topics) {
        final Map<String, Object> request = new HashMap<>();
        request.put("op", "subscribe");
        request.put("args", topics);

        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            client.send(objectMapper.writeValueAsString(request));
            System.out.println("Subscribed to topics: " + String.join(", ", topics));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
