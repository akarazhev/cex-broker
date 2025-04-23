package com.github.akarazhev.cexbroker.kafka;

import com.github.akarazhev.cexbroker.stream.StreamHandler;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Map;

public final class StreamProducer implements StreamHandler {
    private final KafkaProducer<String, Map<String, Object>> producer;

    public StreamProducer() {
        this.producer = new KafkaProducer<>(KafkaConfig.getKafkaProperties());
    }

    @Override
    public void handle(final String topic, final Map<String, Object> data) {
        producer.send(new ProducerRecord<>(topic, data));
    }

    @Override
    public void close() {
        producer.close();
    }
}
