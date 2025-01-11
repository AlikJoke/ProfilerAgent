package ru.joke.profiler.output.sinks.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.io.Closeable;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

final class KafkaProducerSession implements Closeable {

    private static final Logger logger = Logger.getLogger(KafkaProducerSession.class.getCanonicalName());

    private final KafkaSinkConfiguration.ProducerConfiguration configuration;
    private final Producer<String, byte[]> producer;

    KafkaProducerSession(
            final KafkaSinkConfiguration.ProducerConfiguration configuration,
            final KafkaProducer<String, byte[]> producer
    ) {
        this.producer = producer;
        this.configuration = configuration;
    }

    public Producer<String, byte[]> producer() {
        return producer;
    }

    @Override
    public void close() {
        try {
            this.producer.close(Duration.ofMillis(this.configuration.waitOnCloseTimeoutMs()));
        } catch (RuntimeException ex) {
            logger.log(Level.WARNING, "Error on close producer", ex);
        }
    }
}