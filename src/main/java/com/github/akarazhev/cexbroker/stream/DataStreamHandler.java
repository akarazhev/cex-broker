package com.github.akarazhev.cexbroker.stream;

import com.github.akarazhev.cexbroker.Config;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Map;

public final class DataStreamHandler implements DataHandler {
    private final KafkaProducer<String, Map<String, Object>> producer;
    private final String topic;

    public DataStreamHandler(final Config config) {
        this.producer = new KafkaProducer<>(config.getKafkaProperties());
        this.topic = config.getKafkaTopic();
    }

    @Override
    public void handle(final Map<String, Object> object) {
        producer.send(new ProducerRecord<>(topic, object));
    }

    @Override
    public void close() {
        producer.close();
    }
}
