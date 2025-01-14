package com.github.akarazhev.cexbroker.config;

import java.net.URI;
import java.util.Properties;

public final class Config {
    private final String kafkaTopic;
    private final String kafkaBootstrapServers;
    private final String webSocketTopics;

    private static final class Constants {
        private static final class Wss {
            private final static String SPOT_PUBLIC_TESTNET_URL = "wss://stream-testnet.bybit.com/v5/public/spot";
        }
    }

    private Config() {
        String value = System.getenv("KAFKA_TOPIC");
        this.kafkaTopic = value != null ? value : "CEX_BROKER";
        value = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
        this.kafkaBootstrapServers = value != null ? value : "localhost:9092";
        value = System.getenv("WEB_SOCKET_TOPICS");
        this.webSocketTopics = value != null ? value : "tickers.BTCUSDT";
    }

    public static Config getConfig() {
        return new Config();
    }

    public URI getWebSocketUrl() {
        return URI.create(Constants.Wss.SPOT_PUBLIC_TESTNET_URL);
    }

    public String[] getWebSocketTopics() {
        return webSocketTopics.split(",");
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
                ", webSocketTopics='" + webSocketTopics + '\'' +
                '}';
    }
}
