package com.github.akarazhev.cexbroker.kafka;

public interface MessageHandler {

    void handleMessage(final String message);
}
