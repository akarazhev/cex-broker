package com.github.akarazhev.cexbroker.bybit;

public final class Constants {
    private Constants() {
    }

    public static final class Config {
        private Config() {
        }

        public static final class Keys {
            private Keys() {
            }

            public final static String BYBIT_TICKER_TOPICS = "BYBIT_TICKER_TOPICS";
        }

        public static final class Defaults {
            private Defaults() {
            }

            public final static String[] BYBIT_TICKER_TOPICS = new String[]{"tickers.BTCUSDT"};
        }

        public static final class WsPublicStream {
            private WsPublicStream() {
            }

            public final static String SPOT_TEST_URL = "wss://stream-testnet.bybit.com/v5/public/spot";
            public final static String SPOT_MAIN_URL = "wss://stream.bybit.com/v5/public/spot";
        }

        public static final class WsPrivateStream {
            private WsPrivateStream() {
            }

            public final static String TEST_URL = "wss://stream-testnet.bybit.com/v5/private";
            public final static String MAIN_URL = "wss://stream.bybit.com/v5/private";
        }

        public static final class WsOrderEntry {
            private WsOrderEntry() {
            }

            public final static String TEST_URL = "wss://stream-testnet.bybit.com/v5/trade";
            public final static String MAIN_URL = "wss://stream.bybit.com/v5/trade";
        }
    }

    public static final class Filters {
        private Filters() {
        }

        public final static String TOPIC = "topic";
    }

    public static final class Requests {
        private Requests() {
        }

        public final static String SUBSCRIBE = "subscribe";
        public final static String PING = "ping";
    }
}
