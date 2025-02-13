package com.github.akarazhev.cexbroker.stream;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public final class Config {
    private final static Properties PROPERTIES = new Properties();

    static {
        final String value = System.getenv("BOOTSTRAP_SERVERS");
        PROPERTIES.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, value != null ? value : "localhost:9092");
        PROPERTIES.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        PROPERTIES.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Serializer.class.getName());
    }

    private Config() {
        throw new UnsupportedOperationException();
    }

    public static Properties getProperties() {
        return PROPERTIES;
    }

    public static String print() {
        return "Producer Config {properties='" + getProperties() + "'}";
    }
}
