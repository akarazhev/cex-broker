package com.github.akarazhev.cexbroker.bybit;

import com.github.akarazhev.cexbroker.bybit.config.Config;
import com.github.akarazhev.cexbroker.bybit.request.Requests;
import com.github.akarazhev.cexbroker.client.Clients;
import com.github.akarazhev.cexbroker.client.EventListener;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BybitObservable implements ObservableOnSubscribe<String> {
    private final static Logger LOGGER = LoggerFactory.getLogger(BybitObservable.class);
    private final static int MAX_RECONNECT_ATTEMPTS = 10; // Increased for more persistence
    private final static long MAX_RECONNECT_DELAY = 30000; // Max delay of 30 seconds
    private final static long PING_INTERVAL = 20000; // 20 seconds

    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private final Config config;

    private WebSocketClient client;

    private BybitObservable(final Config config) {
        this.config = config;
    }

    public static BybitObservable create(final Config config) {
        return new BybitObservable(config);
    }

    @Override
    public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Throwable {
        final EventListener listener = new EventListener() {
            private Disposable ping;

            @Override
            public void onOpen() {
                reconnectAttempts.set(0); // Reset reconnect attempts on successful connection
                client.send(Requests.ofSubscription(config.getTickerTopics()).toJson());
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
                reconnect("Error occurred: " + (error.getMessage() == null ? error : error.getMessage()));
            }

            private void reconnect(String reason) {
                while (reconnectAttempts.get() < MAX_RECONNECT_ATTEMPTS) {
                    final int attempts = reconnectAttempts.getAndIncrement();
                    final long delay = Math.min(1000 * (long) Math.pow(2, attempts), MAX_RECONNECT_DELAY);
                    LOGGER.warn("{}. Attempting to reconnect in {} ms... (Attempt {})", reason, delay, attempts + 1);
                    try {
                        client = Clients.newWsClient(config.getWebSocketUri(), this);
                        if (client.connectBlocking(delay, TimeUnit.MILLISECONDS)) {
                            LOGGER.warn("Reconnected after {} ms", delay);
                            emitter.setCancellable(client::close);
                            return; // Successfully reconnected, exit the method
                        } else {
                            reason = "Failed to reconnect";
                        }
                    } catch (InterruptedException e) {
                        LOGGER.error("Error reconnecting to the server: {}", e.getMessage());
                        Thread.currentThread().interrupt(); // Restore the interrupt status
                        return; // Exit the reconnection loop if interrupted
                    }
                }
                // If we've exhausted all attempts
                LOGGER.error("Max reconnection attempts reached. Closing observable.");
                emitter.onComplete();
            }

            private void startPing() {
                stopPing(); // Ensure any existing ping is stopped
                ping = Observable.interval(PING_INTERVAL, TimeUnit.MILLISECONDS)
                        .subscribe($ -> sendPing());
            }

            private void stopPing() {
                if (ping != null && !ping.isDisposed()) {
                    ping.dispose();
                }
            }

            private void sendPing() {
                if (client != null && client.isOpen()) {
                    client.send(Requests.ofPing().toJson());
                }
            }
        };

        client = Clients.newWsClient(config.getWebSocketUri(), listener);
        client.connect();
        emitter.setCancellable(client::close);
    }
}
