package com.github.akarazhev.cexbroker.kafka;

import com.github.akarazhev.cexbroker.stream.StreamHandler;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class StreamProducer implements StreamHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamProducer.class);
    private final KafkaProducer<String, Map<String, Object>> producer;

    public StreamProducer() {
        this.producer = new KafkaProducer<>(KafkaConfig.getKafkaProperties());
    }

    @Override
    public void handle(final String topic, final Map<String, Object> data) {
        LOGGER.info("Topic: {}, Data: {}", topic, data);
        producer.send(new ProducerRecord<>(topic, data));
    }

    @Override
    public void close() {
        LOGGER.info("Closing producer...");
        producer.close();
    }
}
