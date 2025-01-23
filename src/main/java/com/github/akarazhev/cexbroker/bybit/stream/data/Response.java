package com.github.akarazhev.cexbroker.bybit.stream.data;

import java.util.Map;

public interface Response {
    Map<String, Object> process(final Map<String, Object> response);
}
