package com.github.akarazhev.cexbroker.bybit;

public final class Constants {
    private Constants() {
        throw new UnsupportedOperationException();
    }

    public static final class Config {
        private Config() {
            throw new UnsupportedOperationException();
        }

        public static final class Keys {
            private Keys() {
                throw new UnsupportedOperationException();
            }

            public final static String BYBIT_TICKER_TOPICS = "BYBIT_TICKER_TOPICS";
        }

        public static final class Defaults {
            private Defaults() {
                throw new UnsupportedOperationException();
            }

            public final static String[] BYBIT_TICKER_TOPICS = new String[]{"tickers.BTCUSDT"};
        }

        public static final class WsPublicStream {
            private WsPublicStream() {
                throw new UnsupportedOperationException();
            }

            public final static String SPOT_TEST_URL = "wss://stream-testnet.bybit.com/v5/public/spot";
            public final static String SPOT_MAIN_URL = "wss://stream.bybit.com/v5/public/spot";
        }

        public static final class WsPrivateStream {
            private WsPrivateStream() {
                throw new UnsupportedOperationException();
            }

            public final static String TEST_URL = "wss://stream-testnet.bybit.com/v5/private";
            public final static String MAIN_URL = "wss://stream.bybit.com/v5/private";
        }

        public static final class WsOrderEntry {
            private WsOrderEntry() {
                throw new UnsupportedOperationException();
            }

            public final static String TEST_URL = "wss://stream-testnet.bybit.com/v5/trade";
            public final static String MAIN_URL = "wss://stream.bybit.com/v5/trade";
        }
    }

    public static final class Topics {
        private Topics() {
            throw new UnsupportedOperationException();
        }

        public final static String STREAM_TOPIC_PREFIX = "bybit";
        public final static String TOPIC_FIELD = "topic";
    }

    public static final class Requests {
        private Requests() {
            throw new UnsupportedOperationException();
        }

        public final static String SUBSCRIBE = "subscribe";
        public final static String PING = "ping";
    }
}
