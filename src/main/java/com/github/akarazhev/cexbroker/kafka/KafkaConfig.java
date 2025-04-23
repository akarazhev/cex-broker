package com.github.akarazhev.cexbroker.kafka;

import java.util.Properties;

public final class KafkaConfig {
    private final static Properties KAFKA_PROPERTIES;

    static {
        KAFKA_PROPERTIES = new Properties();
        final String value = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
        KAFKA_PROPERTIES.put("bootstrap.servers", value != null ? value : "localhost:9092");
        KAFKA_PROPERTIES.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KAFKA_PROPERTIES.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    }

    private KafkaConfig() {
        throw new UnsupportedOperationException();
    }

    public static Properties getKafkaProperties() {
        return KAFKA_PROPERTIES;
    }

    public static String print() {
        return "Kafka Config {" +
                "kafkaProperties='" + getKafkaProperties() + '\'' +
                '}';
    }
}
