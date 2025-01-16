package com.github.akarazhev.cexbroker.net;

public interface EventListener {

    void onOpen();

    void onMessage(final String message);

    void onClose();

    void onError(final Throwable error);
}
