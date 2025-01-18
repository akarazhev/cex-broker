package com.github.akarazhev.cexbroker.stream;

import java.util.Map;

public interface StreamHandler {

    void handle(final String topic, final Map<String, Object> data);

    void close();
}
