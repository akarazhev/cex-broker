package com.github.akarazhev.cexbroker.demo;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class ReconnectingWebSocketListener extends WebSocketListener {
    private final OkHttpClient client;
    private final Request request;
    private WebSocket webSocket;
    private boolean shouldReconnect = true;
    private final String subscribeMessage;

    public ReconnectingWebSocketListener(OkHttpClient client, String wsUrl, String subscribeMessage) {
        this.client = client;
        this.request = new Request.Builder().url(wsUrl).build();
        this.subscribeMessage = subscribeMessage;
    }

    public void connect() {
        webSocket = client.newWebSocket(request, this);
    }

    public void close() {
        shouldReconnect = false;
        if (webSocket != null) {
            webSocket.close(1000, "Closing");
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, @NotNull Response response) {
        System.out.println("WebSocket opened");
        // Send subscription message to Bybit
        webSocket.send(subscribeMessage);
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        System.out.println("Received: " + text);
        // Optionally handle ping/pong or other messages here
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, ByteString bytes) {
        System.out.println("Received bytes: " + bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, @NotNull String reason) {
        System.out.println("WebSocket closing: " + reason);
        webSocket.close(code, reason);
    }

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        System.out.println("WebSocket closed: " + reason);
        if (shouldReconnect) {
            reconnectWithDelay();
        }
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, Throwable t, Response response) {
        System.out.println("WebSocket failure: " + t.getMessage());
        if (shouldReconnect) {
            reconnectWithDelay();
        }
    }

    private void reconnectWithDelay() {
        try {
            TimeUnit.SECONDS.sleep(5); // Wait 5 seconds before reconnecting
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Reconnecting...");
        connect();
    }
}
