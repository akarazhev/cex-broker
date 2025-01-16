package com.github.akarazhev.cexbroker;

import java.util.Properties;

public final class Config {
    private final String kafkaTopic;
    private final String kafkaBootstrapServers;

    private Config() {
        String value = System.getenv("KAFKA_TOPIC");
        this.kafkaTopic = value != null ? value : "CEX_BROKER";
        value = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
        this.kafkaBootstrapServers = value != null ? value : "localhost:9092";
    }

    public static Config getConfig() {
        return new Config();
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }

    public Properties getKafkaProperties() {
        final Properties props = new Properties();
        props.put("bootstrap.servers", kafkaBootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return props;
    }

    @Override
    public String toString() {
        return "Config{" +
                "kafkaTopic='" + kafkaTopic + '\'' +
                ", kafkaBootstrapServers='" + kafkaBootstrapServers + '\'' +
                '}';
    }
}
