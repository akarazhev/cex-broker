package com.github.akarazhev.cexbroker;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DemoConsumer {
    private static final String TOPIC = "bybit.tickers.BTCUSDT";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String GROUP_ID = "test-group";

    public static void main(String[] args) {
        try (Consumer<String, HashMap<String, Object>> consumer = new KafkaConsumer<>(getProperties())) {
            consumer.subscribe(Collections.singletonList(TOPIC));

            while (true) {
                ConsumerRecords<String, HashMap<String, Object>> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, HashMap<String, Object>> record : records) {
                    System.out.printf("Offset = %d, Key = %s%n", record.offset(), record.key());
                    System.out.println("Value:");
                    for (Map.Entry<String, Object> entry : record.value().entrySet()) {
                        System.out.printf("  %s: %s%n", entry.getKey(), entry.getValue());
                    }
                    System.out.println();
                }
            }
        }
    }

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Deserializer.class.getName());
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return props;
    }
}
