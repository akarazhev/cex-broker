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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class NewBybitDataFlow implements FlowableOnSubscribe<String> {
    private static final String WS_URL = "wss://stream-testnet.bybit.com/v5/public/spot";
    private static final int HEARTBEAT_INTERVAL_SEC = 20;
    private static final int PONG_TIMEOUT_SEC = 10;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long MAX_RECONNECT_DELAY_MS = 30000L;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private WebSocket webSocket;
    private ScheduledFuture<?> heartbeatTask;
    private ScheduledFuture<?> pongTimeoutTask;
    private boolean awaitingPong = false;
    private int reconnectAttempts = 0;

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
        reconnectAttempts = 0;
        doConnect(emitter);
        emitter.setCancellable(() -> {
            closeWebSocket();
            stopHeartbeat();
            scheduler.shutdownNow();
        });
    }

    private void doConnect(FlowableEmitter<String> emitter) {
        webSocket = httpClient.newWebSocket(new Request.Builder().url(WS_URL).build(), new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket ws, @NotNull Response response) {
                reconnectAttempts = 0;
                ws.send("{\"op\":\"subscribe\",\"args\":[\"tickers.ETHUSDT\"]}");
                startHeartbeat(ws, emitter);
            }

            @Override
            public void onMessage(@NotNull WebSocket ws, @NotNull String text) {
                if (isSubscriptionConfirmation(text)) startHeartbeat(ws, emitter);
                if (isPongMessage(text)) handlePong();
                else emitter.onNext(text);
            }

            @Override
            public void onMessage(@NotNull WebSocket ws, @NotNull ByteString bytes) {
                onMessage(ws, bytes.utf8());
            }

            @Override
            public void onClosing(@NotNull WebSocket ws, int code, @NotNull String reason) {
                emitter.onComplete();
                stopHeartbeat();
            }

            @Override
            public void onClosed(@NotNull WebSocket ws, int code, @NotNull String reason) {
                stopHeartbeat();
                reconnect(emitter);
            }

            @Override
            public void onFailure(@NotNull WebSocket ws, @NotNull Throwable t, Response response) {
                stopHeartbeat();
                reconnect(emitter);
            }
        });
    }

    private void startHeartbeat(WebSocket ws, FlowableEmitter<String> emitter) {
        stopHeartbeat();
        heartbeatTask = scheduler.scheduleAtFixedRate(() -> {
            if (awaitingPong) {
                reconnect(emitter);
            } else {
                awaitingPong = true;
                ws.send("{\"op\":\"ping\"}");
                pongTimeoutTask = scheduler.schedule(() -> {
                    if (awaitingPong) reconnect(emitter);
                }, PONG_TIMEOUT_SEC, TimeUnit.SECONDS);
            }
        }, HEARTBEAT_INTERVAL_SEC, HEARTBEAT_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    private void stopHeartbeat() {
        if (heartbeatTask != null) heartbeatTask.cancel(true);
        if (pongTimeoutTask != null) pongTimeoutTask.cancel(true);
        heartbeatTask = pongTimeoutTask = null;
        awaitingPong = false;
    }

    private void handlePong() {
        awaitingPong = false;
        if (pongTimeoutTask != null) pongTimeoutTask.cancel(true);
        pongTimeoutTask = null;
    }

    private void reconnect(FlowableEmitter<String> emitter) {
        if (emitter.isCancelled()) return;
        stopHeartbeat();
        closeWebSocket();
        if (++reconnectAttempts <= MAX_RECONNECT_ATTEMPTS) {
            long delay = Math.min(1000L * (1L << reconnectAttempts), MAX_RECONNECT_DELAY_MS);
            scheduler.schedule(() -> doConnect(emitter), delay, TimeUnit.MILLISECONDS);
        } else {
            emitter.onError(new RuntimeException("Max reconnect attempts reached"));
        }
    }

    private void closeWebSocket() {
        if (webSocket != null) {
            webSocket.close(1000, "Client closed");
            webSocket = null;
        }
    }

    private boolean isSubscriptionConfirmation(String msg) {
        return msg.contains("\"op\":\"subscribe\"") && msg.contains("\"success\":true");
    }

    private boolean isPongMessage(String msg) {
        return msg.trim().equalsIgnoreCase("{\"op\":\"pong\"}") || msg.contains("\"ret_msg\":\"pong\"");
    }
}