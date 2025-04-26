package com.github.akarazhev.cexbroker.bybit.stream;

import com.github.akarazhev.cexbroker.bybit.BybitConfig;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.akarazhev.cexbroker.bybit.stream.Responses.isPong;
import static com.github.akarazhev.cexbroker.bybit.stream.Responses.isSubscription;

public class BybitDataFlow2 implements FlowableOnSubscribe<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BybitDataFlow2.class);
    // Use a single shared OkHttpClient for all instances to avoid resource leaks
    private static final OkHttpClient CLIENT = new OkHttpClient();
    private final Request request;
    private volatile WebSocket webSocket;

    private BybitDataFlow2(final String url) {
        this.request = new Request.Builder().url(url).build();
    }

    public static BybitDataFlow2 create() {
        return new BybitDataFlow2(BybitConfig.getWebSocketUri().toString());
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

            public DataFlowListener(final FlowableEmitter<String> emitter) {
                this.emitter = emitter;
                this.awaitingPong = new AtomicBoolean(false);
            }

            @Override
            public void onOpen(final WebSocket ws, final Response response) {
                LOGGER.debug("WebSocket opened: {}", response);
                ws.send(Requests.ofSubscription(BybitConfig.getSubscribeTopics()));
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
                    LOGGER.debug("Received message: {}", text); // Lowered log level
                    emitter.onNext(text);
                }
            }

            @Override
            public void onClosed(final WebSocket ws, int code, final String reason) {
                LOGGER.warn("WebSocket closed with code: {}, reason: {}", code, reason);
                cleanup();
                if (!emitter.isCancelled()) {
                    LOGGER.warn("Reconnecting because of closed connection...");
                    sleepWithBackoff(attempt);
                    connect(emitter, attempt + 1);
                } else {
                    emitter.onComplete();
                }
            }

            @Override
            public void onFailure(final WebSocket ws, final Throwable t, final Response response) {
                LOGGER.error("WebSocket error: {}", t.getMessage(), t);
                cleanup();
                if (!emitter.isCancelled()) {
                    LOGGER.warn("Reconnecting because of error...");
                    sleepWithBackoff(attempt);
                    connect(emitter, attempt + 1);
                } else {
                    emitter.onError(t);
                }
            }

            private void handleSubscription(final WebSocket ws) {
                stopPing();
                pingDisposable = Flowable.interval(
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
                long backoff = Math.min(1000L * (1L << attempt), 30000L); // exponential, max 30s
                try {
                    TimeUnit.MILLISECONDS.sleep(backoff);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        webSocket = CLIENT.newWebSocket(request, new DataFlowListener(emitter));
        emitter.setCancellable(() -> {
            if (webSocket != null) {
                LOGGER.info("WebSocket closing...");
                webSocket.close(1000, "Cancelled");
                webSocket = null;
            }
            // No client shutdown here since CLIENT is shared
        });
    }
}
