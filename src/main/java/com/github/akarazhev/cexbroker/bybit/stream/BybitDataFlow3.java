package com.github.akarazhev.cexbroker.bybit.stream;

import com.github.akarazhev.cexbroker.bybit.BybitConfig;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.akarazhev.cexbroker.bybit.stream.Responses.isPong;
import static com.github.akarazhev.cexbroker.bybit.stream.Responses.isSubscription;


public class BybitDataFlow3 implements FlowableOnSubscribe<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BybitDataFlow3.class);
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private static final long MAX_BACKOFF_MS = 30000L;
    private static final Random RANDOM = new Random();

    // Allow client injection for testability/configurability
    private static OkHttpClient sharedClient;
    private final OkHttpClient client;
    private final Request request;
    private volatile WebSocket webSocket;

    // For production, use shared client
    private BybitDataFlow3(final String url) {
        this(url, getOrCreateSharedClient());
    }

    // For testing or custom config
    public BybitDataFlow3(final String url, OkHttpClient client) {
        this.client = client;
        this.request = new Request.Builder().url(url).build();
    }

    public static BybitDataFlow3 create() {
        return new BybitDataFlow3(BybitConfig.getWebSocketUri().toString());
    }

    private static OkHttpClient getOrCreateSharedClient() {
        if (sharedClient == null) {
            synchronized (BybitDataFlow3.class) {
                if (sharedClient == null) {
                    sharedClient = new OkHttpClient();
                }
            }
        }
        return sharedClient;
    }

    @Override
    public void subscribe(@NonNull FlowableEmitter<String> emitter) {
        connect(emitter, 0);
    }

    private void connect(final FlowableEmitter<String> emitter, final int attempt) {
        class DataFlowListener extends WebSocketListener {
            private final FlowableEmitter<String> emitter;
            private final AtomicBoolean awaitingPong;
            private Disposable pingDisposable;
            private boolean subscribed = false;

            public DataFlowListener(final FlowableEmitter<String> emitter) {
                this.emitter = emitter;
                this.awaitingPong = new AtomicBoolean(false);
            }

            @Override
            public void onOpen(final WebSocket ws, final Response response) {
                LOGGER.debug("WebSocket opened: {}", response);
                // Defensive: only subscribe once per connection
                if (!subscribed) {
                    ws.send(Requests.ofSubscription(BybitConfig.getSubscribeTopics()));
                    subscribed = true;
                }
            }

            @Override
            public void onMessage(final WebSocket ws, final String text) {
                if (isSubscription(text)) {
                    LOGGER.debug("Received subscription message: {}", text);
                    handleSubscription(ws);
                } else if (isPong(text)) {
                    LOGGER.debug("Received pong message: {}", text);
                    awaitingPong.set(false);
                } else {
                    LOGGER.debug("Received message: {}", text);
                    emitter.onNext(text);
                }
            }

            @Override
            public void onClosed(final WebSocket ws, int code, final String reason) {
                LOGGER.warn("WebSocket closed: code={}, reason={}", code, reason);
                cleanup();
                if (!emitter.isCancelled()) {
                    if (attempt < MAX_RECONNECT_ATTEMPTS) {
                        LOGGER.warn("Reconnecting attempt {}/{}...", attempt + 1, MAX_RECONNECT_ATTEMPTS);
                        sleepWithBackoff(attempt);
                        connect(emitter, attempt + 1);
                    } else {
                        LOGGER.error("Max reconnect attempts reached, completing emitter.");
                        emitter.onComplete();
                    }
                } else {
                    emitter.onComplete();
                }
            }

            @Override
            public void onFailure(final WebSocket ws, final Throwable t, final Response response) {
                LOGGER.error("WebSocket error: {}", t.getMessage(), t);
                cleanup();
                if (!emitter.isCancelled()) {
                    if (attempt < MAX_RECONNECT_ATTEMPTS) {
                        LOGGER.warn("Reconnecting attempt {}/{}...", attempt + 1, MAX_RECONNECT_ATTEMPTS);
                        sleepWithBackoff(attempt);
                        connect(emitter, attempt + 1);
                    } else {
                        LOGGER.error("Max reconnect attempts reached, propagating error.");
                        emitter.onError(t);
                    }
                } else {
                    emitter.onError(t);
                }
            }

            private void handleSubscription(final WebSocket ws) {
                stopPing();
                pingDisposable = io.reactivex.rxjava3.core.Flowable.interval(
                                BybitConfig.getPingInterval(), TimeUnit.MILLISECONDS)
                        .subscribe($ -> sendPing(ws), t -> LOGGER.error("Heartbeat error", t));
            }

            private void sendPing(final WebSocket ws) {
                if (awaitingPong.get()) {
                    LOGGER.warn("Previous ping didn't receive a pong. Reconnecting...");
                    cleanup();
                    ws.close(1000, "Ping timeout");
                    connect(emitter, 0);
                } else {
                    LOGGER.debug("Sending ping");
                    ws.send(Requests.ofPing());
                    awaitingPong.set(true);
                }
            }

            private void stopPing() {
                if (pingDisposable != null && !pingDisposable.isDisposed()) {
                    pingDisposable.dispose();
                    awaitingPong.set(false);
                }
            }

            private void cleanup() {
                stopPing();
                if (webSocket != null) {
                    webSocket.cancel();
                    webSocket = null;
                }
            }

            private void sleepWithBackoff(int attempt) {
                long baseBackoff = Math.min(1000L * (1L << attempt), MAX_BACKOFF_MS);
                long jitter = RANDOM.nextInt(500); // up to 500ms jitter
                long backoff = baseBackoff + jitter;
                try {
                    TimeUnit.MILLISECONDS.sleep(backoff);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        webSocket = client.newWebSocket(request, new DataFlowListener(emitter));
        emitter.setCancellable(() -> {
            if (webSocket != null) {
                LOGGER.info("WebSocket closing...");
                webSocket.close(1000, "Cancelled");
                webSocket = null;
            }
            // No client shutdown here since client may be shared
        });
    }

    // Optional: for graceful shutdown of shared client at app exit
    public static void shutdownSharedClient() {
        if (sharedClient != null) {
            sharedClient.dispatcher().executorService().shutdown();
            sharedClient.connectionPool().evictAll();
            if (sharedClient.cache() != null) {
                try {
                    sharedClient.cache().close();
                } catch (Exception e) {
                    LOGGER.warn("Error closing OkHttpClient cache", e);
                }
            }
            sharedClient = null;
        }
    }
}
