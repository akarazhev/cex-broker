package com.github.akarazhev.cexbroker.bybit;

public final class BybitConstants {
    private BybitConstants() {
        throw new UnsupportedOperationException();
    }

    public static final class WebSocket {
        private WebSocket() {
            throw new UnsupportedOperationException();
        }

        public final static String PUBLIC_TESTNET_SPOT = "bybit.public.testnet.spot";
        public final static String PUBLIC_TICKER_TOPICS = "bybit.public.ticker.topics";
    }

    public static final class Topic {
        private Topic() {
            throw new UnsupportedOperationException();
        }

        public final static String STREAM_TOPIC_PREFIX = "bybit";
        public final static String TOPIC_FIELD = "topic";
    }

    public static final class Request {
        private Request() {
            throw new UnsupportedOperationException();
        }

        public final static String SUBSCRIBE = "subscribe";
        public final static String PING = "ping";
    }
}
