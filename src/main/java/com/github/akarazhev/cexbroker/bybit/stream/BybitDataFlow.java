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
            private final AtomicBoolean isAwaitingPong;
            private Disposable ping;

            public DataFlowListener() {
                this.isAwaitingPong = new AtomicBoolean(false);
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
                    ping = Flowable.interval(BybitConfig.getPingInterval(), TimeUnit.MILLISECONDS)
                            .subscribe($ -> {
                                if (isAwaitingPong.get()) {
                                    LOGGER.warn("Previous ping didn't receive a pong.");
                                    closeWsStopPing(ws, "Ping timeout");
                                } else {
                                    LOGGER.debug("Sending ping");
                                    ws.send(Requests.ofPing());
                                    isAwaitingPong.set(true);
                                }
                            }, t -> LOGGER.error("Heartbeat error", t));
                } else if (isPong(text)) {
                    LOGGER.debug("Received pong message: {}", text);
                    isAwaitingPong.set(false);
                } else {
                    if (!emitter.isCancelled()) {
                        LOGGER.debug("Received message: {}", text);
                        emitter.onNext(text);
                    }
                }
            }

            @Override
            public void onMessage(final WebSocket ws, final ByteString bytes) {
                if (!emitter.isCancelled()) {
                    LOGGER.debug("Received bytes: {}", bytes.hex());
                    onMessage(ws, bytes.utf8());
                }
            }

            @Override
            public void onClosing(final WebSocket ws, int code, final String reason) {
                closeWsStopPing(ws, "Connection closing");
                if (!emitter.isCancelled()) {
                    LOGGER.warn("Reconnecting because of closing connection with the code: {}, reason: {}", code, reason);
                    reconnect();
                }
            }

            @Override
            public void onClosed(final WebSocket ws, int code, final String reason) {
                closeWsStopPing(ws, "Connection closed");
                if (emitter.isCancelled()) {
                    LOGGER.warn("Connection closed with the code: {}, reason: {}", code, reason);
                    emitter.onComplete();
                }
            }

            @Override
            public void onFailure(final WebSocket ws, final Throwable t, final Response response) {
                closeWsStopPing(ws, "Connection failed");
                if (emitter.isCancelled()) {
                    LOGGER.error("Connection failed: {}", t.getMessage());
                    emitter.onError(t);
                } else {
                    LOGGER.warn("Reconnecting because of failure: {}", t.getMessage());
                    reconnect();
                }
            }

            private void reconnect() {
                client.connectionPool().evictAll();

                try {
                    TimeUnit.MILLISECONDS.sleep(BybitConfig.getReconnectInterval());
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                connect(emitter);
            }

            private void closeWsStopPing(final WebSocket ws, final String reason) {
                ws.close(1000, reason);
                if (ping != null && !ping.isDisposed()) {
                    LOGGER.warn("Stop pinging...");
                    ping.dispose();
                    isAwaitingPong.set(false);
                }
            }
        }

        final var url = BybitConfig.getWebSocketUri().toString();
        webSocket = client.newWebSocket(new Request.Builder().url(url).build(), new DataFlowListener());
        emitter.setCancellable(() -> {
            if (emitter.isCancelled()) {
                if (webSocket != null) {
                    LOGGER.info("WebSocket closing...");
                    webSocket.close(1000, "Connection closed");
                }

                LOGGER.info("Shutting down the client...");
                client.dispatcher().executorService().shutdown();
            }
        });
    }
}
