package com.github.akarazhev.cexbroker.stream;

import java.util.Properties;

public final class Config {
    private final static String KAFKA_BOOTSTRAP_SERVERS;
    private final static String KAFKA_TOPIC;
    private final static Properties KAFKA_PROPERTIES;

    static {
        String value = System.getenv("KAFKA_TOPIC");
        KAFKA_TOPIC = value != null ? value : "CEX_BROKER";

        value = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
        KAFKA_BOOTSTRAP_SERVERS = value != null ? value : "localhost:9092";

        KAFKA_PROPERTIES = new Properties();
        KAFKA_PROPERTIES.put("bootstrap.servers", KAFKA_BOOTSTRAP_SERVERS);
        KAFKA_PROPERTIES.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KAFKA_PROPERTIES.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    }

    private Config() {
        throw new UnsupportedOperationException();
    }

    public static String getKafkaTopic() {
        return KAFKA_TOPIC;
    }

    public static String getKafkaBootstrapServers() {
        return KAFKA_BOOTSTRAP_SERVERS;
    }

    public static Properties getKafkaProperties() {
        return KAFKA_PROPERTIES;
    }

    public static String print() {
        return "Kafka Config {" +
                "kafkaBootstrapServers='" + KAFKA_BOOTSTRAP_SERVERS + '\'' +
                ", kafkaTopic='" + KAFKA_TOPIC + '\'' +
                '}';
    }
}
