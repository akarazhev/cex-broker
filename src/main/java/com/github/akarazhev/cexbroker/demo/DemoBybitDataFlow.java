package com.github.akarazhev.cexbroker.demo;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class DemoBybitDataFlow implements FlowableOnSubscribe<String> {
    private final OkHttpClient client;
    private final String wsUrl;
    private final String subscribeMessage;
    private volatile boolean shouldReconnect = true;
    private WebSocket webSocket;

    public DemoBybitDataFlow(String wsUrl, String subscribeMessage) {
        this.client = new OkHttpClient();
        this.wsUrl = wsUrl;
        this.subscribeMessage = subscribeMessage;
    }

    @Override
    public void subscribe(FlowableEmitter<String> emitter) {
        shouldReconnect = true;
        connect(emitter);

        emitter.setCancellable(() -> {
            shouldReconnect = false;
            if (webSocket != null) {
                webSocket.close(1000, "Cancelled");
            }
        });
    }

    private void connect(FlowableEmitter<String> emitter) {
        Request request = new Request.Builder().url(wsUrl).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket ws, @NotNull Response response) {
                ws.send(subscribeMessage);
            }

            @Override
            public void onMessage(@NotNull WebSocket ws, @NotNull String text) {
                emitter.onNext(text);
            }

            @Override
            public void onMessage(@NotNull WebSocket ws, @NotNull ByteString bytes) {
                // emitter.onNext(bytes.utf8()); // Uncomment if you want to handle binary messages as strings
            }

            @Override
            public void onClosing(@NotNull WebSocket ws, int code, @NotNull String reason) {
                ws.close(code, reason);
            }

            @Override
            public void onClosed(@NotNull WebSocket ws, int code, @NotNull String reason) {
                if (shouldReconnect && !emitter.isCancelled()) {
                    reconnectWithDelay(emitter);
                } else {
                    emitter.onComplete();
                }
            }

            @Override
            public void onFailure(@NotNull WebSocket ws, @NotNull Throwable t, Response response) {
                if (shouldReconnect && !emitter.isCancelled()) {
                    reconnectWithDelay(emitter);
                } else {
                    emitter.onError(t);
                }
            }

            private void reconnectWithDelay(FlowableEmitter<String> emitter) {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (!emitter.isCancelled()) {
                    connect(emitter);
                }
            }
        });
    }

    // Factory method for consumer
    public static Flowable<String> create(String wsUrl, String subscribeMessage) {
        return Flowable.create(new DemoBybitDataFlow(wsUrl, subscribeMessage), BackpressureStrategy.BUFFER);
    }
}
