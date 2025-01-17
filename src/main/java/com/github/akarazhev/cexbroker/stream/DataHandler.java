package com.github.akarazhev.cexbroker.stream;

import java.util.Map;

public interface DataHandler {

    void handle(final Map<String, Object> object);

    void close();
}
