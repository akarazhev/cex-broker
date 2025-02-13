package com.github.akarazhev.cexbroker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.akarazhev.cexbroker.core.Command;
import com.github.akarazhev.cexbroker.core.Commands;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CexBroker {
    private static final Logger LOGGER = LoggerFactory.getLogger(CexBroker.class);
    private static final long POLL_TIMEOUT_MS = 100;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static volatile boolean running = true;

    private final Commands commands;

    public CexBroker() {
        this.commands = Commands.init();
    }

    private void handleMessage(String jsonMessage) {
        try {
            Map<String, String> commandMap = OBJECT_MAPPER.readValue(jsonMessage, Map.class);
            String commandType = commandMap.get("type");
            String message = commandMap.get("message");

            if (commandType != null && message != null) {
                Command command = commands.create(commandType, message);
                if (command != null) {
                    command.execute();
                } else {
                    LOGGER.warn("Unknown command type: {}", commandType);
                }
            } else {
                LOGGER.warn("Invalid JSON format: {}", jsonMessage);
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing JSON message", e);
        }
    }

    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        String groupId = "my-group";
        String topic = "my-topic";

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        CexBroker commandConsumer = new CexBroker();

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("Shutting down consumer...");
                running = false;
                consumer.wakeup();
            }));

            try {
                while (running) {
                    consumer.poll(Duration.ofMillis(POLL_TIMEOUT_MS))
                            .forEach(r -> commandConsumer.handleMessage(r.value()));
                }
            } catch (WakeupException e) {
                if (running) {
                    throw e;
                }
            } finally {
                consumer.close();
                LOGGER.info("Consumer closed.");
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error", e);
        }
    }
}