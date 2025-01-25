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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class BybitDataFlow implements FlowableOnSubscribe<String> {
    private final static Logger LOGGER = LoggerFactory.getLogger(BybitDataFlow.class);
    // Reconnection constants
    private final static int MAX_RECONNECT_ATTEMPTS = 10; // Increased for more persistence
    private final static long MAX_RECONNECT_DELAY = 30000; // Max delay of 30 seconds
    // Ping constants
    private static final long PING_INTERVAL = 20000; // 20 seconds
    private static final long PONG_TIMEOUT = 10000; // 10 seconds

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
            private final AtomicBoolean awaitingPong = new AtomicBoolean(false);
            private Disposable ping;
            private Disposable pongTimeout;

            @Override
            public void onOpen() {
                reconnectAttempts.set(0);
                client.send(Requests.ofSubscription(Config.getTickerTopics()).toJson());
                startHeartbeat();
            }

            @Override
            public void onMessage(final String message) {
                if (isPongMessage(message)) {
                    handlePong();
                } else {
                    emitter.onNext(message);
                }
            }

            @Override
            public void onClose() {
                stopHeartbeat();
                reconnect("Connection closed");
            }

            @Override
            public void onError(final Throwable error) {
                stopHeartbeat();
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

            private void startHeartbeat() {
                stopHeartbeat();
                ping = Flowable.interval(PING_INTERVAL, TimeUnit.MILLISECONDS).subscribe($ -> sendPing());
            }

            private void stopHeartbeat() {
                if (ping != null && !ping.isDisposed()) {
                    ping.dispose();
                }

                cancelPongTimeout();
            }

            private void sendPing() {
                if (client != null && client.isOpen()) {
                    if (awaitingPong.get()) {
                        LOGGER.warn("Previous ping didn't receive a pong. Reconnecting...");
                        reconnect("Ping timeout");
                    } else {
                        client.send(Requests.ofPing().toJson());
                        awaitingPong.set(true);
                        schedulePongTimeout();
                    }
                }
            }

            private void schedulePongTimeout() {
                cancelPongTimeout();
                pongTimeout = Flowable.timer(PONG_TIMEOUT, TimeUnit.MILLISECONDS).subscribe($ -> {
                    if (awaitingPong.get()) {
                        LOGGER.warn("Pong not received within timeout. Reconnecting...");
                        reconnect("Pong timeout");
                    }
                });
            }

            private void cancelPongTimeout() {
                if (pongTimeout != null && !pongTimeout.isDisposed()) {
                    pongTimeout.dispose();
                }
            }

            private void handlePong() {
                awaitingPong.set(false);
                cancelPongTimeout();
            }

            private boolean isPongMessage(final String message) {
                // Implement logic to check if the message is a pong response
                // This depends on the specific format of Bybit's pong messages
                return message.contains("pong") || message.contains("\"ret_msg\":\"pong\"");
            }
        };

        client = Clients.newWsClient(Config.getWebSocketUri(), listener);
        client.connect();
        emitter.setCancellable(client::close);
    }
}
