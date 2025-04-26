package com.github.akarazhev.cexbroker.bybit.stream;

final class Responses {

    private Responses() {
        throw new UnsupportedOperationException();
    }

    public static boolean isSubscription(final String text) {
        return text.contains("\"success\":true") && text.contains("\"op\":\"subscribe\"");
    }

    public static boolean isPong(final String text) {
        return text.contains("{\"op\":\"pong\"}") || text.contains("\"ret_msg\":\"pong\"");
    }
}
