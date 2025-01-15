package com.github.akarazhev.cexbroker.bybit.request;

public final class Ping {
    private final String op;

    public Ping() {
        this.op = "ping";
    }

    public String toJson() {
        return "{\"op\":\"" + op + "\"}";
    }
}
