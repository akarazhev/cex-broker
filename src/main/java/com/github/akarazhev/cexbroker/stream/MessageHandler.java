package com.github.akarazhev.cexbroker.stream;

public interface MessageHandler {

    void handleMessage(final String message);
}
