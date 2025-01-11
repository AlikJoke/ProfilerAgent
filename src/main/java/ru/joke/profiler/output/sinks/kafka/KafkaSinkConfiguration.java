package ru.joke.profiler.output.sinks.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import ru.joke.profiler.configuration.InvalidConfigurationException;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.util.MapConfigurationPropertiesParser;
import ru.joke.profiler.configuration.util.MillisTimePropertyParser;
import ru.joke.profiler.output.sinks.async.AsyncOutputDataSinkConfigurationSupport;
import ru.joke.profiler.output.sinks.async.AsyncSinkDataFlushingConfiguration;
import ru.joke.profiler.output.sinks.util.parsers.OutputDataPropertiesMappingConfigurationPropertyParser;
import ru.joke.profiler.output.sinks.util.recovery.ConnectionRecoveryConfiguration;

import java.util.Collections;
import java.util.Map;

import static ru.joke.profiler.output.sinks.kafka.OutputDataKafkaSinkHandle.SINK_TYPE;
import static ru.joke.profiler.util.ArgUtil.*;

public final class KafkaSinkConfiguration extends AsyncOutputDataSinkConfigurationSupport {

    private static final String KAFKA_SINK_PROPERTIES_PREFIX = SINK_TYPE + ".";

    private final ProducerConfiguration producerConfiguration;
    private final OutputRecordConfiguration outputRecordConfiguration;
    private final ConnectionRecoveryConfiguration recoveryConfiguration;

    @ProfilerConfigurationPropertiesWrapper(prefix = KAFKA_SINK_PROPERTIES_PREFIX)
    public KafkaSinkConfiguration(
            final ProducerConfiguration producerConfiguration,
            final OutputRecordConfiguration outputRecordConfiguration,
            final ConnectionRecoveryConfiguration recoveryConfiguration,
            final AsyncSinkDataFlushingConfiguration asyncFlushingConfiguration
    ) {
        super(asyncFlushingConfiguration);
        this.producerConfiguration = checkNotNull(producerConfiguration, "producerConfiguration");
        this.outputRecordConfiguration = checkNotNull(outputRecordConfiguration, "outputRecordConfiguration");
        this.recoveryConfiguration = checkNotNull(recoveryConfiguration, "recoveryConfiguration");
    }

    public ProducerConfiguration producerConfiguration() {
        return producerConfiguration;
    }

    public OutputRecordConfiguration outputRecordConfiguration() {
        return outputRecordConfiguration;
    }

    public ConnectionRecoveryConfiguration recoveryConfiguration() {
        return recoveryConfiguration;
    }

    @Override
    public String toString() {
        return "KafkaSinkConfiguration{"
                + "producerConfiguration=" + producerConfiguration
                + ", outputRecordConfiguration=" + outputRecordConfiguration
                + ", recoveryConfiguration=" + recoveryConfiguration
                + ", asyncFlushingConfiguration=" + asyncFlushingConfiguration
                + '}';
    }

    public static class ProducerConfiguration {

        private static final String PRODUCER_PROPERTIES_PREFIX = "producer.";

        static final String DISABLE_CLUSTER_VALIDATION_ON_START = "disable_cluster_validation_on_start";
        static final String DISABLE_COMPRESSION = "disable_compression";
        static final String WAIT_ON_CLOSE = "wait_on_close_timeout";

        private final Map<String, String> producerProperties;
        private final boolean useCompression;
        private final long waitOnCloseTimeoutMs;
        private final boolean checkClusterOnStart;

        @ProfilerConfigurationPropertiesWrapper(prefix = PRODUCER_PROPERTIES_PREFIX)
        public ProducerConfiguration(
                @ProfilerConfigurationPropertiesWrapper(parser = MapConfigurationPropertiesParser.class) final Map<String, String> producerProperties,
                @ProfilerConfigurationProperty(name = DISABLE_COMPRESSION) final boolean disableCompression,
                @ProfilerConfigurationProperty(name = WAIT_ON_CLOSE, defaultValue = "30s", parser = MillisTimePropertyParser.class) final long waitOnCloseTimeoutMs,
                @ProfilerConfigurationProperty(name = DISABLE_CLUSTER_VALIDATION_ON_START) final boolean disableClusterValidationOnStart
        ) {
            this.producerProperties = checkNotNull(producerProperties, "producerProperties");
            this.useCompression = !disableCompression;
            this.waitOnCloseTimeoutMs = checkNonNegative(waitOnCloseTimeoutMs, "waitOnCloseTimeoutMs");
            this.checkClusterOnStart = !disableClusterValidationOnStart;

            if (producerProperties.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG) == null) {
                throw new InvalidConfigurationException(String.format("'%s' property is required to connect to Kafka", ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
            }
        }

        public Map<String, String> producerProperties() {
            return producerProperties;
        }

        public boolean useCompression() {
            return useCompression;
        }

        public long waitOnCloseTimeoutMs() {
            return waitOnCloseTimeoutMs;
        }

        public boolean checkClusterOnStart() {
            return checkClusterOnStart;
        }

        @Override
        public String toString() {
            return "ProducerConfiguration{"
                    + "producerProperties=" + producerProperties
                    + ", useCompression=" + useCompression
                    + ", waitOnCloseTimeoutMs=" + waitOnCloseTimeoutMs
                    + ", checkClusterOnStart=" + checkClusterOnStart
                    + '}';
        }
    }

    public static class OutputRecordConfiguration {

        private static final String OUTPUT_RECORD_CONFIGURATION_PREFIX = "output-record.";

        private static final String OUTPUT_QUEUE = "target_queue";
        private static final String MESSAGE_TYPE = "message_type";
        private static final String MESSAGE_TYPE_HEADER = "message_type_header";
        private static final String BODY_MAPPING = "body_mapping";
        private static final String HEADERS_MAPPING = "headers_mapping";

        private final String outputQueue;
        private final String messageType;
        private final String messageTypeHeader;
        private final Map<String, String> propertiesMapping;
        private final Map<String, String> headersMapping;

        @ProfilerConfigurationPropertiesWrapper(prefix = OUTPUT_RECORD_CONFIGURATION_PREFIX)
        public OutputRecordConfiguration(
                @ProfilerConfigurationProperty(name = OUTPUT_QUEUE, required = true) final String outputQueue,
                @ProfilerConfigurationProperty(name = MESSAGE_TYPE, defaultValue = "profiling-data") final String messageType,
                @ProfilerConfigurationProperty(name = MESSAGE_TYPE_HEADER, defaultValue = "MessageType") final String messageTypeHeader,
                @ProfilerConfigurationProperty(name = BODY_MAPPING, parser = OutputDataPropertiesMappingConfigurationPropertyParser.class) final Map<String, String> propertiesMapping,
                @ProfilerConfigurationProperty(name = HEADERS_MAPPING, parser = OutputDataPropertiesMappingConfigurationPropertyParser.class) final Map<String, String> headersMapping
        ) {
            this.outputQueue = checkNotEmpty(outputQueue, "outputQueue");
            this.messageType = checkNotEmpty(messageType, "messageType");
            this.messageTypeHeader = checkNotNull(messageTypeHeader, "messageTypeHeader");
            this.headersMapping = Collections.unmodifiableMap(headersMapping);
            this.propertiesMapping = Collections.unmodifiableMap(propertiesMapping);
        }

        public String messageTypeHeader() {
            return messageTypeHeader;
        }

        public String messageType() {
            return messageType;
        }

        public Map<String, String> propertiesMapping() {
            return propertiesMapping;
        }

        public Map<String, String> headersMapping() {
            return headersMapping;
        }

        public String outputQueue() {
            return outputQueue;
        }

        @Override
        public String toString() {
            return "OutputRecordConfiguration{"
                    + "outputQueue='" + outputQueue + '\''
                    + ", messageType='" + messageType + '\''
                    + ", messageTypeHeader='" + messageTypeHeader + '\''
                    + ", propertiesMapping=" + propertiesMapping
                    + ", headersMapping=" + headersMapping
                    + '}';
        }
    }
}
