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

public final class BybitDataFlow implements FlowableOnSubscribe<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BybitDataFlow.class);
    private final OkHttpClient client;
    private final Request request;
    private WebSocket webSocket;

    private BybitDataFlow(final String url) {
        this.client = new OkHttpClient();
        this.request = new Request.Builder().url(url).build();
    }

    public static BybitDataFlow create() {
        return new BybitDataFlow(BybitConfig.getWebSocketUri().toString());
    }

    @Override
    public void subscribe(@NonNull FlowableEmitter<String> emitter) throws Throwable {
        connect(emitter);
    }

    private void connect(final FlowableEmitter<String> emitter) {
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
                    LOGGER.info("Received message: {}", text);
                    emitter.onNext(text);
                }
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

            private void handleSubscription(final WebSocket ws) {
                pingDisposable = Flowable.interval(BybitConfig.getPingInterval(), TimeUnit.MILLISECONDS)
                        .subscribe($ -> sendPing(ws), t -> LOGGER.error("Heartbeat error", t));
            }

            private void sendPing(final WebSocket ws) {
                if (awaitingPong.get()) {
                    LOGGER.warn("Previous ping didn't receive a pong. Reconnecting...");
                    stopPing();
                    ws.close(1000, "Ping timeout");
                    connect(emitter);
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

            private void sleep() {
                try {
                    TimeUnit.MILLISECONDS.sleep(BybitConfig.getReconnectInterval());
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        webSocket = client.newWebSocket(request, new DataFlowListener(emitter));
        emitter.setCancellable(() -> {
            if (emitter.isCancelled()) {
                if (webSocket != null) {
                    LOGGER.info("WebSocket closing...");
                    webSocket.close(1000, "Cancelled");
                    webSocket = null;
                }

                LOGGER.info("Shutting down client...");
                client.dispatcher().executorService().shutdown();
            }
        });
    }
}
