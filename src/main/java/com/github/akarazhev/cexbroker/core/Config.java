package com.github.akarazhev.cexbroker.core;

import com.github.akarazhev.cexbroker.stream.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public final class Config {
    private final static Properties PROPERTIES = new Properties();

    static {
        final String value = System.getenv("BOOTSTRAP_SERVERS");
        PROPERTIES.put("bootstrap.servers", value != null ? value : "localhost:9092");
        PROPERTIES.put("key.serializer", StringSerializer.class.getName());
        PROPERTIES.put("value.serializer", Serializer.class.getName());
    }

    private Config() {
        throw new UnsupportedOperationException();
    }

    public static Properties getProperties() {
        return PROPERTIES;
    }

    public static String print() {
        return "Consumer Config {properties='" + getProperties() + "'}";
    }
}
