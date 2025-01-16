package com.github.akarazhev.cexbroker.stream;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public final class KafkaMessageHandler implements MessageHandler {
    private final KafkaProducer<String, String> producer;
    private final String topic;

    public KafkaMessageHandler(final KafkaProducer<String, String> producer, final String topic) {
        this.producer = producer;
        this.topic = topic;
    }

    @Override
    public void handleMessage(final String message) {
        final ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
        producer.send(record);
    }
}
