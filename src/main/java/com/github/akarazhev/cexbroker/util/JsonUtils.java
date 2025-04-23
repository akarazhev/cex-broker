package com.github.akarazhev.cexbroker.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.util.Map;

public final class JsonUtils {
    private static final ObjectMapper MAPPER;
    private static final ObjectReader READER;
    private static final ObjectWriter WRITER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        READER = MAPPER.reader();
        WRITER = MAPPER.writer();
    }

    private JsonUtils() {
        throw new UnsupportedOperationException();
    }

    public static String objectToJson(final Object object) {
        try {
            return WRITER.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> jsonToMap(final String json) {
        try {
            return READER.forType(Map.class).readValue(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> byte[] objectToBytes(final T object) {
        try {
            return WRITER.writeValueAsBytes(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
