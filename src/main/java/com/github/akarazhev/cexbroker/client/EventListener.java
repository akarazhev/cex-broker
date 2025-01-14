package com.github.akarazhev.cexbroker.client;

import org.java_websocket.client.WebSocketClient;

public interface EventListener {
    void setClient(final WebSocketClient client);

    void onOpen();

    void onMessage(final String message);

    void onClose();

    void onError(final Throwable error);
}
