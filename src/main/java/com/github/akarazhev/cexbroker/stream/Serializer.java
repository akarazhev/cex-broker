package com.github.akarazhev.cexbroker.stream;

import com.github.akarazhev.cexbroker.util.JsonUtils;
import org.apache.kafka.common.KafkaException;

import java.io.IOException;

public final class Serializer<T> implements org.apache.kafka.common.serialization.Serializer<T> {

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }

        try {
            return JsonUtils.objectToBytes(data);
        } catch (IOException e) {
            throw new KafkaException("Failed to serialize data", e);
        }
    }
}
