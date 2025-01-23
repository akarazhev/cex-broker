package com.github.akarazhev.cexbroker.bybit.stream.data;

final class Constants {

    private Constants() {
        throw new UnsupportedOperationException();
    }

    public static final class Field {

        private Field() {
            throw new UnsupportedOperationException();
        }

        public static final String DATA = "data";
        public static final String TYPE = "type";
        public static final String SPOT_TYPE = "spot";
        public static final String TS = "ts";
        public static final String CS = "cs";
    }
}
