package com.github.akarazhev.cexbroker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class Application {

    private static final String WEBSOCKET_URL = "wss://stream-testnet.bybit.com/v5/public/linear";
    private static final String TOPIC = "tickers.BTCUSDT";

    public static void main(String[] args) {
        Observable<String> wsObservable = Observable.create(emitter -> {
            WebSocketClient ws = new WebSocketClient(new URI(WEBSOCKET_URL)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("WebSocket opened");
                    Application.onOpen(this, TOPIC);
                }

                @Override
                public void onMessage(String message) {
                    emitter.onNext(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("### about to close ###");
                    emitter.onComplete();
                }

                @Override
                public void onError(Exception ex) {
                    emitter.onError(ex);
                }
            };

            ws.connect();
            emitter.setCancellable(() -> {
                if (ws != null) {
                    ws.close();
                }
            });
        });

        Disposable disposable = wsObservable
                .map(Application::receive)
                .subscribe(
                        jsonNode -> System.out.println(jsonNode.toString()),
                        throwable -> System.err.println("Error: " + throwable),
                        () -> System.out.println("WebSocket closed")
                );

        // Keep the program running
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        disposable.dispose();
    }

    private static void onOpen(WebSocketClient ws, String topic) {
        Map<String, Object> subscribeRequest = new HashMap<>();
        subscribeRequest.put("op", "subscribe");
        subscribeRequest.put("args", new String[]{topic});

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String subscribeMessage = objectMapper.writeValueAsString(subscribeRequest);
            ws.send(subscribeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JsonNode receive(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(message);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
