package com.github.akarazhev.cexbroker.bybit.stream;

import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class NewBybitDataFlow implements FlowableOnSubscribe<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewBybitDataFlow.class);

    private static final String WS_URL = "wss://stream-testnet.bybit.com/v5/public/spot"; // Replace with your endpoint
    private static final int HEARTBEAT_INTERVAL_SEC = 20;
    private static final int PONG_TIMEOUT_SEC = 10;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long MAX_RECONNECT_DELAY_MS = 30000L;

    private final OkHttpClient httpClient = new OkHttpClient.Builder().build();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicReference<WebSocket> webSocketRef = new AtomicReference<>(null);
    private final Lock reconnectLock = new ReentrantLock();
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);

    private ScheduledFuture<?> heartbeatTask;
    private ScheduledFuture<?> pongTimeoutTask;

    private NewBybitDataFlow() {
    }

    public static NewBybitDataFlow create() {
        return new NewBybitDataFlow();
    }

    @Override
    public void subscribe(FlowableEmitter<String> emitter) {
        connect(emitter);
    }

    private void connect(FlowableEmitter<String> emitter) {
        reconnectAttempts.set(0);
        doConnect(emitter);
    }

    private void doConnect(FlowableEmitter<String> emitter) {
        Request request = new Request.Builder().url(WS_URL).build();
        WebSocketListener listener = new OkHttpWebSocketListener(emitter);
        WebSocket ws = httpClient.newWebSocket(request, listener);
        webSocketRef.set(ws);

        emitter.setCancellable(() -> {
            WebSocket socket = webSocketRef.getAndSet(null);
            if (socket != null) {
                socket.close(1000, "Client closed");
            }

            stopHeartbeat();
            scheduler.shutdownNow();
        });
    }

    private class OkHttpWebSocketListener extends WebSocketListener {
        private final FlowableEmitter<String> emitter;
        private final AtomicBoolean awaitingPong = new AtomicBoolean(false);

        OkHttpWebSocketListener(FlowableEmitter<String> emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onOpen(WebSocket webSocket, @NotNull Response response) {
            LOGGER.info("WebSocket connection opened");
            reconnectAttempts.set(0);

            // Example: subscribe to ETHUSDT ticker (adjust as needed)
            String subscribeMsg = "{\"op\":\"subscribe\",\"args\":[\"tickers.ETHUSDT\"]}";
            LOGGER.debug("Sending subscription: {}", subscribeMsg);
            webSocket.send(subscribeMsg);

            startHeartbeat(webSocket);
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            LOGGER.debug("Received message: {}", text);
            if (isSubscriptionConfirmation(text)) {
                startHeartbeat(webSocket);
            }

            if (isPongMessage(text)) {
                LOGGER.debug("Received pong message");
                handlePong();
            } else {
                emitter.onNext(text);
            }
        }

        private boolean isSubscriptionConfirmation(String message) {
            // Bybit sends {"op":"subscribe","req_id":"...","success":true,...}
            return message.contains("\"op\":\"subscribe\"") && message.contains("\"success\":true");
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, ByteString bytes) {
            LOGGER.warn("Received unexpected binary message, converting to UTF-8");
            onMessage(webSocket, bytes.utf8());
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            LOGGER.warn("WebSocket is closing: {} - {}", code, reason);
            emitter.onComplete();
            stopHeartbeat();
        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            LOGGER.warn("WebSocket closed: {} - {}", code, reason);
            stopHeartbeat();
            reconnect("Closed: " + reason, emitter);
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
            LOGGER.error("WebSocket failure", t);
            stopHeartbeat();
            reconnect("Failure: " + t.getMessage(), emitter);
        }

        private void startHeartbeat(WebSocket webSocket) {
            stopHeartbeat();
            heartbeatTask = scheduler.scheduleAtFixedRate(() -> {
                if (awaitingPong.get()) {
                    LOGGER.warn("Pong not received, reconnecting...");
                    reconnect("Pong timeout", emitter);
                } else {
                    awaitingPong.set(true);
                    String pingMsg = "{\"op\":\"ping\"}";
                    LOGGER.debug("Sending ping: {}", pingMsg);
                    webSocket.send(pingMsg);
                    pongTimeoutTask = scheduler.schedule(() -> {
                        if (awaitingPong.get()) {
                            LOGGER.warn("Pong timeout, reconnecting...");
                            reconnect("Pong timeout", emitter);
                        }
                    }, PONG_TIMEOUT_SEC, TimeUnit.SECONDS);
                }
            }, HEARTBEAT_INTERVAL_SEC, HEARTBEAT_INTERVAL_SEC, TimeUnit.SECONDS);
        }

        private void stopHeartbeat() {
            if (heartbeatTask != null) {
                heartbeatTask.cancel(true);
                heartbeatTask = null;
            }

            if (pongTimeoutTask != null) {
                pongTimeoutTask.cancel(true);
                pongTimeoutTask = null;
            }

            awaitingPong.set(false);
        }

        private void handlePong() {
            awaitingPong.set(false);
            if (pongTimeoutTask != null) {
                pongTimeoutTask.cancel(true);
                pongTimeoutTask = null;
            }
        }

        private boolean isPongMessage(String message) {
            // Example: stricter JSON-based pong detection
            return message.trim().equalsIgnoreCase("{\"op\":\"pong\"}")
                    || message.contains("\"ret_msg\":\"pong\"");
        }

        private void reconnect(String reason, FlowableEmitter<String> emitter) {
            if (emitter.isCancelled()) {
                return;
            }

            if (reconnectLock.tryLock()) {
                try {
                    stopHeartbeat();
                    WebSocket oldWs = webSocketRef.getAndSet(null);
                    if (oldWs != null) {
                        oldWs.close(1000, "Reconnecting");
                    }

                    int attempts = reconnectAttempts.incrementAndGet();
                    if (attempts <= MAX_RECONNECT_ATTEMPTS) {
                        long delay = Math.min(1000L * (1L << attempts), MAX_RECONNECT_DELAY_MS);
                        LOGGER.warn("{} Attempting to reconnect in {} ms (Attempt {})", reason, delay, attempts);
                        scheduler.schedule(() -> doConnect(emitter), delay, TimeUnit.MILLISECONDS);
                    } else {
                        LOGGER.error("Max reconnect attempts reached. Giving up.");
                        emitter.onError(new RuntimeException("Max reconnect attempts reached"));
                    }
                } finally {
                    reconnectLock.unlock();
                }
            }
        }
    }

    private void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel(true);
            heartbeatTask = null;
        }

        if (pongTimeoutTask != null) {
            pongTimeoutTask.cancel(true);
            pongTimeoutTask = null;
        }
    }
}