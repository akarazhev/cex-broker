package com.github.akarazhev.cexbroker.bybit.request;

public record Ping(String op) {

    public String toJson() {
        return "{\"op\":\"" + op + "\"}";
    }
}
