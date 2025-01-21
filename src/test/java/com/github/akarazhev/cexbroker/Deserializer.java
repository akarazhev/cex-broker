package com.github.akarazhev.cexbroker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class Deserializer implements org.apache.kafka.common.serialization.Deserializer<HashMap<String, Object>> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Nothing to configure
    }

    @Override
    public HashMap<String, Object> deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.readValue(data, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing to HashMap", e);
        }
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
