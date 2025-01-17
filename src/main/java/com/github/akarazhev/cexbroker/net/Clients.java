package com.github.akarazhev.cexbroker.net;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public final class Clients {

    private Clients() {
        throw new UnsupportedOperationException();
    }

    public static WebSocketClient newWsClient(final URI uri, final EventListener listener) {
        return new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                listener.onOpen();
            }

            @Override
            public void onMessage(String message) {
                listener.onMessage(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                listener.onClose();
            }

            @Override
            public void onError(Exception ex) {
                listener.onError(ex);
            }
        };
    }
}
