package com.github.akarazhev.cexbroker.stream;

import com.github.akarazhev.cexbroker.util.JsonUtils;

import java.util.Map;

public final class Serializer<T> implements org.apache.kafka.common.serialization.Serializer<T> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }

        try {
            return JsonUtils.objectToBytes(data);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing JSON message", e);
        }
    }

    @Override
    public void close() {
    }
}
