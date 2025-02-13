package com.github.akarazhev.cexbroker;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CexBrokerClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(CexBrokerClient.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final KafkaProducer<String, String> producer;
    private final String topic;

    public CexBrokerClient(String bootstrapServers, String topic) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        this.producer = new KafkaProducer<>(props);
        this.topic = topic;
    }

    public void sendCommand(String type, String message) {
        try {
            String jsonMessage = OBJECT_MAPPER.writeValueAsString(
                    Map.of("type", type, "message", message));

            ProducerRecord<String, String> record = new ProducerRecord<>(topic, jsonMessage);
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    LOGGER.info("Message sent successfully. Topic: {}, Partition: {}, Offset: {}", metadata.topic(),
                            metadata.partition(), metadata.offset());
                } else {
                    LOGGER.error("Error sending message", exception);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error creating JSON message", e);
        }
    }

    public void close() {
        producer.close();
        LOGGER.info("Producer closed.");
    }

    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        String topic = "my-topic";

        CexBrokerClient client = new CexBrokerClient(bootstrapServers, topic);

        try {
            // Send a PRINT command
            client.sendCommand("PRINT", "Hello, CexBroker!");

            // Send a LOG command
            client.sendCommand("LOG", "This is a log message");

            // Send an unknown command type
            client.sendCommand("UNKNOWN", "This command type doesn't exist");

        } finally {
            client.close();
        }
    }
}
