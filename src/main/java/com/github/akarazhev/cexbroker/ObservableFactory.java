package com.github.akarazhev.cexbroker;

import com.github.akarazhev.cexbroker.bybit.request.Requests;
import com.github.akarazhev.cexbroker.client.ClientFactory;
import com.github.akarazhev.cexbroker.client.EventListener;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ObservableFactory {

    public static Observable<String> createBybitObservable(final URI uri, final String[] topics) {
        return Observable.create(emitter -> {
            final Logger logger = LoggerFactory.getLogger(Application.class);
            final AtomicInteger reconnectAttempts = new AtomicInteger(0);
            final int maxReconnectAttempts = 10; // Increased for more persistence
            final long maxReconnectDelay = 30000; // Max delay of 30 seconds
            final long pingInterval = 20000; // 20 seconds

            final EventListener listener = new EventListener() {
                private WebSocketClient client;
                private Disposable pingDisposable;

                @Override
                public void setClient(final WebSocketClient client) {
                    this.client = client;
                }

                @Override
                public void onOpen() {
                    reconnectAttempts.set(0); // Reset reconnect attempts on successful connection
                    client.send(Requests.subscription(topics).toJson());
                    startPing();
                }

                @Override
                public void onMessage(final String message) {
                    emitter.onNext(message);
                }

                @Override
                public void onClose() {
                    stopPing();
                    reconnect("Connection closed");
                }

                @Override
                public void onError(final Throwable error) {
                    stopPing();
                    reconnect("Error occurred: " + error.getMessage());
                }

                private void reconnect(final String reason) {
                    final int attempts = reconnectAttempts.getAndIncrement();
                    if (attempts < maxReconnectAttempts) {
                        final long delay = Math.min(1000 * (long) Math.pow(2, attempts), maxReconnectDelay);
                        logger.warn("{}. Attempting to reconnect in {} ms... (Attempt {})", reason, delay, attempts + 1);
                        emitter.onNext(reason + ". Reconnecting... (Attempt " + (attempts + 1) + ")");

                        Observable.timer(delay, TimeUnit.MILLISECONDS)
                                .subscribe($ -> {
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

                private void startPing() {
                    stopPing(); // Ensure any existing ping is stopped
                    pingDisposable = Observable.interval(pingInterval, TimeUnit.MILLISECONDS)
                            .subscribe($ -> sendPing());
                }

                private void stopPing() {
                    if (pingDisposable != null && !pingDisposable.isDisposed()) {
                        pingDisposable.dispose();
                    }
                }

                private void sendPing() {
                    if (client != null && client.isOpen()) {
                        client.send(Requests.ping().toJson());
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
