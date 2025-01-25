package com.github.akarazhev.cexbroker.bybit.stream;

import com.github.akarazhev.cexbroker.bybit.config.Config;
import com.github.akarazhev.cexbroker.bybit.request.Requests;
import com.github.akarazhev.cexbroker.net.Clients;
import com.github.akarazhev.cexbroker.net.EventListener;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class BybitDataFlow implements FlowableOnSubscribe<String> {
    private final static Logger LOGGER = LoggerFactory.getLogger(BybitDataFlow.class);
    private final static int MAX_RECONNECT_ATTEMPTS = 10; // Increased for more persistence
    private final static long MAX_RECONNECT_DELAY = 30000; // Max delay of 30 seconds
    private final static long PING_INTERVAL = 20000; // 20 seconds

    private final Lock reconnectLock = new ReentrantLock();
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);

    private WebSocketClient client;

    private BybitDataFlow() {
    }

    public static BybitDataFlow create() {
        return new BybitDataFlow();
    }

    @Override
    public void subscribe(@NonNull FlowableEmitter<String> emitter) throws Throwable {
        final EventListener listener = new EventListener() {
            private Disposable ping;

            @Override
            public void onOpen() {
                reconnectAttempts.set(0); // Reset reconnect attempts on successful connection
                client.send(Requests.ofSubscription(Config.getTickerTopics()).toJson());
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
                if (reconnectLock.tryLock()) {
                    try {
                        while (reconnectAttempts.get() < MAX_RECONNECT_ATTEMPTS) {
                            final int attempts = reconnectAttempts.getAndIncrement();
                            final long delay = Math.min(1000 * (long) Math.pow(2, attempts), MAX_RECONNECT_DELAY);
                            LOGGER.warn("{}. Attempting to reconnect in {} ms... (Attempt {})", reason, delay, attempts + 1);
                            try {
                                 client = Clients.newWsClient(Config.getWebSocketUri(), this);
                                if (client.connectBlocking()) {
                                    LOGGER.warn("Reconnected after {} ms", delay);
                                    emitter.setCancellable(client::close);
                                    return; // Successfully reconnected, exit the method
                                } else {
                                    reason = "Failed to reconnect";
                                    Thread.sleep(Duration.ofMillis(delay));
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
                    } finally {
                        reconnectLock.unlock();
                    }
                }
            }

            private void startPing() {
                stopPing(); // Ensure any existing ping is stopped
                ping = Flowable.interval(PING_INTERVAL, TimeUnit.MILLISECONDS)
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

        client = Clients.newWsClient(Config.getWebSocketUri(), listener);
        client.connect();
        emitter.setCancellable(client::close);
    }
}
