package com.github.akarazhev.cexbroker.bybit.stream;

import com.github.akarazhev.cexbroker.bybit.BybitConfig;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.akarazhev.cexbroker.bybit.BybitConfig.getPingInterval;

public final class OldBybitDataFlow implements FlowableOnSubscribe<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OldBybitDataFlow.class);
    private final Lock reconnectLock = new ReentrantLock();
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private final AtomicReference<WebSocketClient> client = new AtomicReference<>(null);

    private OldBybitDataFlow() {
    }

    public static OldBybitDataFlow create() {
        return new OldBybitDataFlow();
    }

    @Override
    public void subscribe(@NonNull FlowableEmitter<String> emitter) throws Throwable {
        LOGGER.info("Subscribing to BybitDataFlow");
        final EventListener listener = new EventListener() {
            private final AtomicBoolean awaitingPong = new AtomicBoolean(false);
            private Disposable ping;
            private Disposable pongTimeout;

            @Override
            public void onOpen() {
                LOGGER.info("WebSocket connection opened");
                reconnectAttempts.set(0);
                final String subscriptionRequest = Requests.ofSubscription(BybitConfig.getSubscribeTopics());
                // Avoid logging sensitive data
                LOGGER.debug("Sending subscription request (length={}): [REDACTED]", subscriptionRequest.length());
                WebSocketClient ws = client.get();
                if (ws != null && ws.isOpen()) {
                    ws.send(subscriptionRequest);
                }

                startHeartbeat();
            }

            @Override
            public void onMessage(final String message) {
                if (isPongMessage(message)) {
                    LOGGER.debug("Received pong message");
                    handlePong();
                } else {
                    LOGGER.debug("Received message: {}", message);
                    emitter.onNext(message);
                }
            }

            @Override
            public void onClose() {
                LOGGER.warn("WebSocket connection closed");
                stopHeartbeat();
                reconnect("Connection closed");
            }

            @Override
            public void onError(final Throwable error) {
                LOGGER.error("WebSocket error occurred", error);
                stopHeartbeat();
                reconnect("Error occurred: " + (error.getMessage() == null ? error : error.getMessage()));
            }

            private void reconnect(String reason) {
                if (reconnectLock.tryLock()) {
                    try {
                        stopHeartbeat();
                        WebSocketClient oldClient = client.getAndSet(null);
                        if (oldClient != null && oldClient.isOpen()) {
                            try {
                                oldClient.close();
                            } catch (Exception e) {
                                LOGGER.warn("Error closing old WebSocketClient: {}", e.getMessage());
                            }
                        }

                        while (reconnectAttempts.get() < 10) {
                            final int attempts = reconnectAttempts.getAndIncrement();
                            final long delay = Math.min(1000L * (1L << attempts), 30000);
                            LOGGER.warn("{}. Attempting to reconnect in {} ms... (Attempt {})", reason, delay, attempts + 1);
                            try {
                                WebSocketClient newClient = Clients.ofWebSocket(BybitConfig.getWebSocketUri(), this);
                                client.set(newClient);
                                if (newClient.connectBlocking()) {
                                    LOGGER.warn("Reconnected after {} ms", delay);
                                    emitter.setCancellable(() -> {
                                        LOGGER.info("Cancelling subscription and closing WebSocket client");
                                        WebSocketClient c = client.getAndSet(null);
                                        if (c != null && c.isOpen()) c.close();
                                    });
                                    return; // Successfully reconnected, exit the method
                                } else {
                                    reason = "Failed to reconnect";
                                    Thread.sleep(Duration.ofMillis(delay).toMillis());
                                }
                            } catch (InterruptedException e) {
                                LOGGER.error("Error reconnecting to the server: {}", e.getMessage());
                                Thread.currentThread().interrupt(); // Restore the interrupt status
                                return; // Exit the reconnection loop if interrupted
                            } catch (Exception e) {
                                LOGGER.error("Unexpected error during reconnect: {}", e.getMessage());
                            }
                        }
                        // If we've exhausted all attempts
                        LOGGER.error("Max reconnection attempts reached. Closing observable.");
                        emitter.onComplete();
                    } finally {
                        reconnectLock.unlock();
                    }
                } else {
                    LOGGER.warn("Reconnection already in progress, skipping this attempt");
                }
            }

            private void startHeartbeat() {
                LOGGER.debug("Starting heartbeat");
                stopHeartbeat();
                ping = Flowable.interval(getPingInterval(), TimeUnit.MILLISECONDS)
                        .subscribe($ -> sendPing(), err -> LOGGER.error("Heartbeat error", err));
            }

            private void stopHeartbeat() {
                LOGGER.debug("Stopping heartbeat");
                if (ping != null && !ping.isDisposed()) {
                    ping.dispose();
                }

                cancelPongTimeout();
            }

            private void sendPing() {
                WebSocketClient ws = client.get();
                if (ws != null && ws.isOpen()) {
                    if (awaitingPong.get()) {
                        LOGGER.warn("Previous ping didn't receive a pong. Reconnecting...");
                        reconnect("Ping timeout");
                    } else {
                        LOGGER.debug("Sending ping");
                        ws.send(Requests.ofPing());
                        awaitingPong.set(true);
                        schedulePongTimeout();
                    }
                }
            }

            private void schedulePongTimeout() {
                cancelPongTimeout();
                pongTimeout = Flowable.timer(10000, TimeUnit.MILLISECONDS)
                        .subscribe($ -> {
                            if (awaitingPong.get()) {
                                LOGGER.warn("Pong not received within timeout. Reconnecting...");
                                reconnect("Pong timeout");
                            }
                        }, err -> LOGGER.error("Pong timeout error", err));
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
                // Example: stricter JSON-based pong detection
                return message.trim().equalsIgnoreCase("{\"op\":\"pong\"}")
                        || message.contains("\"ret_msg\":\"pong\"");
            }
        };

        LOGGER.info("Initializing WebSocket client");
        WebSocketClient wsClient = Clients.ofWebSocket(BybitConfig.getWebSocketUri(), listener);
        client.set(wsClient);
        wsClient.connect();

        emitter.setCancellable(() -> {
            LOGGER.info("Cancelling subscription and closing WebSocket client");
            WebSocketClient c = client.getAndSet(null);
            if (c != null && c.isOpen()) {
                c.close();
            }
            // Heartbeat cleanup is handled by listener.onClose()
        });
    }
}