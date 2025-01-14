package com.github.akarazhev.cexbroker.bybit.request;

public final class Ping {
    private final String op;

    public Ping() {
        this.op = "ping";
    }

    public String getOp() {
        return op;
    }

    public String toJson() {
        return "{\"op\":\"" + op + "\"}";
    }
}
