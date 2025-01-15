package com.github.akarazhev.cexbroker.bybit.config;

final class Constants {
    private Constants() {
    }

    static final class Keys {
        private Keys() {
        }

        final static String BYBIT_TICKER_TOPICS = "BYBIT_TICKER_TOPICS";
    }

    static final class Defaults {
        private Defaults() {
        }

        final static String[] BYBIT_TICKER_TOPICS = new String[]{"tickers.BTCUSDT"};
    }

    static final class WsPublicStream {
        private WsPublicStream() {
        }

        final static String SPOT_TEST_URL = "wss://stream-testnet.bybit.com/v5/public/spot";
        final static String SPOT_MAIN_URL = "wss://stream.bybit.com/v5/public/spot";
    }

    static final class WsPrivateStream {
        private WsPrivateStream() {
        }

        final static String TEST_URL = "wss://stream-testnet.bybit.com/v5/private";
        final static String MAIN_URL = "wss://stream.bybit.com/v5/private";
    }

    static final class WsOrderEntry {
        private WsOrderEntry() {
        }

        final static String TEST_URL = "wss://stream-testnet.bybit.com/v5/trade";
        final static String MAIN_URL = "wss://stream.bybit.com/v5/trade";
    }
}
