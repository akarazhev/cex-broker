package com.github.akarazhev.cexbroker;

import java.net.URI;
import java.util.Properties;

public final class Config {
    private final String kafkaTopic;
    private final String kafkaBootstrapServers;
    private final String webSocketTopics;

    private static final class Constants {
        private final static String WEB_SOCKET_TESTNET_URL = "wss://stream-testnet.bybit.com/v5/public/linear";
    }

    private Config() {
        this.kafkaTopic = System.getenv("KAFKA_TOPIC");
        this.kafkaBootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
        this.webSocketTopics = System.getenv("WEB_SOCKET_TOPICS");
    }

    public static Config getConfig() {
        return new Config();
    }

    public URI getWebSocketUrl() {
        return URI.create(Constants.WEB_SOCKET_TESTNET_URL);
    }

    public String[] getWebSocketTopics() {
        return new String[]{"tickers.BTCUSDT"};
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
}
