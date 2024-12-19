package ru.joke.profiler.core.output.handlers.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

final class KafkaProducerSessionFactory {

    private static final int DEFAULT_RETRIES = 2;
    private static final int DEFAULT_LINGER_MS = 200;
    private static final int DEFAULT_BATCH_SIZE = 64 * 1024;
    private static final int DEFAULT_MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION = 100;
    private static final int DEFAULT_DELIVERY_TIMEOUT_MS = 120_000;
    private static final int DEFAULT_REQUEST_TIMEOUT_MS = 60_000;

    private static final String DEFAULT_COMPRESSION_TYPE = "lz4";
    private static final String DEFAULT_ACKS = "1";
    private static final String DEFAULT_ENABLE_IDEMPOTENCE = "false";
    private static final String DEFAULT_CLIENT_ID = "joke-profiler";

    KafkaProducerSession create(final KafkaSinkConfiguration.ProducerConfiguration configuration) {
        final Map<String, Object> properties = composePropertiesMap(configuration);
        final KafkaProducer<String, byte[]> session = new KafkaProducer<>(
                properties,
                new StringSerializer(),
                new ByteArraySerializer()
        );

        return new KafkaProducerSession(configuration, session);
    }

    private Map<String, Object> composePropertiesMap(final KafkaSinkConfiguration.ProducerConfiguration configuration) {
        final Map<String, Object> finalProperties = new HashMap<>(configuration.producerProperties());

        finalProperties.putIfAbsent(ProducerConfig.RETRIES_CONFIG, DEFAULT_RETRIES);
        finalProperties.putIfAbsent(ProducerConfig.PARTITIONER_CLASS_CONFIG, MessageKeyPartitioner.class.getCanonicalName());
        finalProperties.putIfAbsent(ProducerConfig.LINGER_MS_CONFIG, DEFAULT_LINGER_MS);
        finalProperties.putIfAbsent(ProducerConfig.BATCH_SIZE_CONFIG, DEFAULT_BATCH_SIZE);
        finalProperties.putIfAbsent(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, DEFAULT_MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION);
        if (configuration.useCompression()) {
            finalProperties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, DEFAULT_COMPRESSION_TYPE);
        } else {
            finalProperties.remove(ProducerConfig.COMPRESSION_TYPE_CONFIG);
        }

        finalProperties.putIfAbsent(ProducerConfig.ACKS_CONFIG, DEFAULT_ACKS);
        finalProperties.putIfAbsent(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, DEFAULT_ENABLE_IDEMPOTENCE);
        finalProperties.putIfAbsent(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, DEFAULT_DELIVERY_TIMEOUT_MS);
        finalProperties.putIfAbsent(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, DEFAULT_REQUEST_TIMEOUT_MS);
        finalProperties.putIfAbsent(ProducerConfig.CLIENT_ID_CONFIG, DEFAULT_CLIENT_ID);

        return finalProperties;
    }
}
