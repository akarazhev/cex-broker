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
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.akarazhev.cexbroker.bybit.stream.Responses.isPong;
import static com.github.akarazhev.cexbroker.bybit.stream.Responses.isSubscription;

public final class BybitDataFlow implements FlowableOnSubscribe<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BybitDataFlow.class);
    private final OkHttpClient client;
    private WebSocket webSocket;

    private BybitDataFlow() {
        this.client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    public static BybitDataFlow create() {
        return new BybitDataFlow();
    }

    @Override
    public void subscribe(@NonNull final FlowableEmitter<String> emitter) throws Throwable {
        connect(emitter);
    }

    private void connect(final FlowableEmitter<String> emitter) {
        final class DataFlowListener extends WebSocketListener {
            private final AtomicBoolean awaitingPong;
            private Disposable pingDisposable;

            public DataFlowListener() {
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
                    startPing(ws);
                } else if (isPong(text)) {
                    LOGGER.debug("Received pong message: {}", text);
                    awaitingPong.set(false);
                } else {
                    LOGGER.info("Received message: {}", text);
                    emitter.onNext(text);
                }
            }

            @Override
            public void onMessage(final WebSocket ws, final ByteString bytes) {
                LOGGER.debug("Received bytes: {}", bytes.hex());
                emitter.onNext(bytes.utf8());
            }

            @Override
            public void onClosed(final WebSocket ws, int code, final String reason) {
                if (emitter.isCancelled()) {
                    LOGGER.warn("WebSocket closed with code: {}, reason: {}", code, reason);
                    stopPing();
                    ws.close(1000, "Cancelled");
                    emitter.onComplete();
                } else {
                    LOGGER.warn("Reconnecting because of closed connection...");
                    sleep();
                    connect(emitter);
                }
            }

            @Override
            public void onFailure(final WebSocket ws, final Throwable t, final Response response) {
                if (emitter.isCancelled()) {
                    LOGGER.error("WebSocket error: {}", t.getMessage());
                    stopPing();
                    ws.close(1000, "Cancelled");
                    emitter.onError(t);
                } else {
                    LOGGER.warn("Reconnecting because of error...");
                    sleep();
                    connect(emitter);
                }
            }

            private void startPing(final WebSocket ws) {
                pingDisposable = Flowable.interval(BybitConfig.getPingInterval(), TimeUnit.MILLISECONDS)
                        .subscribe($ -> sendPing(ws), t -> LOGGER.error("Heartbeat error", t));
            }

            private void stopPing() {
                if (pingDisposable != null && !pingDisposable.isDisposed()) {
                    pingDisposable.dispose();
                    awaitingPong.set(false);
                }
            }

            private void sendPing(final WebSocket ws) {
                if (awaitingPong.get()) {
                    LOGGER.warn("Previous ping didn't receive a pong. Reconnecting...");
                    stopPing();
                    ws.close(1000, "Ping timeout");
                } else {
                    LOGGER.debug("Sending ping");
                    ws.send(Requests.ofPing());
                    awaitingPong.set(true);
                }
            }

            private void sleep() {
                try {
                    TimeUnit.MILLISECONDS.sleep(BybitConfig.getReconnectInterval());
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        final var request = new Request.Builder().url(BybitConfig.getWebSocketUri().toString()).build();
        webSocket = client.newWebSocket(request, new DataFlowListener());
        emitter.setCancellable(() -> {
            if (emitter.isCancelled()) {
                if (webSocket != null) {
                    LOGGER.info("WebSocket closing...");
                    webSocket.close(1000, "Cancelled");
                }

                LOGGER.info("Shutting down client...");
                client.dispatcher().executorService().shutdown();
            }
        });
    }
}
