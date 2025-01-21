package com.github.akarazhev.cexbroker.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public final class JsonUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);
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

    public static Map<String, Object> jsonToMap(final String json) throws IOException {
        return READER.forType(Map.class).readValue(json);
    }

    public static <T> byte[] objectToBytes(final T object) throws IOException {
        return WRITER.writeValueAsBytes(object);
    }
}
