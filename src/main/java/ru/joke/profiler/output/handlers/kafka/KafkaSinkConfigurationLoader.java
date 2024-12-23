package ru.joke.profiler.output.handlers.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import ru.joke.profiler.configuration.InvalidConfigurationException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ru.joke.profiler.configuration.ConfigurationProperties.*;

final class KafkaSinkConfigurationLoader {

    private static final long DEFAULT_RECOVERY_MAX_RETRY_INTERVAL_MS = TimeUnit.SECONDS.toMillis(30);
    private static final long DEFAULT_WAIT_ON_CLOSE_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(30);
    private static final String DEFAULT_MESSAGE_TYPE = "profiling-data";
    private static final String DEFAULT_MESSAGE_TYPE_HEADER = "MessageType";

    KafkaSinkConfiguration load(final Map<String, String> properties) {

        final KafkaSinkConfiguration.ProducerConfiguration producerConfiguration = loadProducerConfiguration(properties);
        final KafkaSinkConfiguration.OutputMessageConfiguration outputMessageConfiguration = loadOutputMessageConfiguration(properties);
        final KafkaSinkConfiguration.ConnectionRecoveryConfiguration recoveryConfiguration = loadRecoveryConfiguration(properties);

        return new KafkaSinkConfiguration(
                producerConfiguration,
                outputMessageConfiguration,
                recoveryConfiguration
        );
    }

    private KafkaSinkConfiguration.ProducerConfiguration loadProducerConfiguration(final Map<String, String> properties) {

        final Map<String, String> producerProperties = new HashMap<>();
        properties.forEach((p, v) -> {
            if (p.startsWith(STATIC_KAFKA_SINK_PRODUCER_PROPERTIES_PREFIX)
                    && !p.equals(STATIC_KAFKA_SINK_PRODUCER_WAIT_ON_CLOSE_MS)
                    && !p.equals(STATIC_KAFKA_SINK_PRODUCER_DISABLE_COMPRESSION)) {
                final String propertyName = p.substring(STATIC_KAFKA_SINK_PRODUCER_PROPERTIES_PREFIX.length());
                producerProperties.put(propertyName, v);
            }
        });

        if (producerProperties.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG) == null) {
            throw new InvalidConfigurationException(String.format("'%s' property is required to connect to Kafka", ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        }

        final boolean validateClusterOnStart = !parseBooleanProperty(properties, STATIC_KAFKA_SINK_PRODUCER_DISABLE_CLUSTER_VALIDATION_ON_START);
        final boolean useCompression = !parseBooleanProperty(properties, STATIC_KAFKA_SINK_PRODUCER_DISABLE_COMPRESSION);
        final long waitOnCloseTimeout = parseLongProperty(properties, STATIC_KAFKA_SINK_PRODUCER_WAIT_ON_CLOSE_MS, DEFAULT_WAIT_ON_CLOSE_TIMEOUT_MS);

        return new KafkaSinkConfiguration.ProducerConfiguration(
                producerProperties,
                useCompression,
                waitOnCloseTimeout,
                validateClusterOnStart
        );
    }

    private KafkaSinkConfiguration.OutputMessageConfiguration loadOutputMessageConfiguration(final Map<String, String> properties) {

        final String outputQueue = findRequiredProperty(properties, STATIC_KAFKA_SINK_MESSAGE_OUTPUT_QUEUE);
        final String messageType = properties.getOrDefault(STATIC_KAFKA_SINK_MESSAGE_TYPE, DEFAULT_MESSAGE_TYPE);
        final String messageTypeHeader = properties.getOrDefault(STATIC_KAFKA_SINK_MESSAGE_TYPE_HEADER, DEFAULT_MESSAGE_TYPE_HEADER);

        final String propertiesMappingStr = properties.get(STATIC_KAFKA_SINK_MESSAGE_PROPERTIES_MAPPING);
        final Map<String, String> propertiesMapping = createMapping(propertiesMappingStr);

        final String headersMappingStr = properties.get(STATIC_KAFKA_SINK_MESSAGE_HEADERS_MAPPING);
        final Map<String, String> headersMapping = createMapping(headersMappingStr);

        return new KafkaSinkConfiguration.OutputMessageConfiguration(
                outputQueue,
                messageType,
                messageTypeHeader,
                propertiesMapping,
                headersMapping
        );
    }

    private KafkaSinkConfiguration.ConnectionRecoveryConfiguration loadRecoveryConfiguration(final Map<String, String> properties) {
        final long recoveryTimeoutMs = parseLongProperty(properties, STATIC_KAFKA_SINK_RECOVERY_TIMEOUT_MS, Long.MAX_VALUE);
        final long maxRetryIntervalMs = parseLongProperty(properties, STATIC_KAFKA_SINK_RECOVERY_MAX_RETRY_INTERVAL_MS, DEFAULT_RECOVERY_MAX_RETRY_INTERVAL_MS);

        final String processingPolicyStr = properties.get(STATIC_KAFKA_SINK_RECOVERY_PROCESSING_POLICY);
        final ProcessingInRecoveryStatePolicy policy = ProcessingInRecoveryStatePolicy.parse(processingPolicyStr);

        return new KafkaSinkConfiguration.ConnectionRecoveryConfiguration(
                recoveryTimeoutMs,
                maxRetryIntervalMs,
                policy
        );
    }

    private Map<String, String> createMapping(final String mappingString) {
        if (mappingString == null || mappingString.isEmpty()) {
            return Collections.emptyMap();
        }

        return Arrays.stream(mappingString.split(";"))
                        .map(mappingParts -> mappingParts.split(":"))
                        .collect(Collectors.toMap(mapping -> mapping[0], mapping -> mapping[mapping.length - 1]));
    }
}
