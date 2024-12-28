package ru.joke.profiler.output.handlers.jms;

import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.util.MillisTimePropertyParser;
import ru.joke.profiler.output.handlers.util.parsers.OutputDataPropertiesMappingConfigurationPropertyParser;
import ru.joke.profiler.output.handlers.util.pool.ConnectionPoolConfiguration;
import ru.joke.profiler.output.handlers.util.recovery.ConnectionRecoveryConfiguration;

import java.util.Map;

import static ru.joke.profiler.output.handlers.jms.OutputDataJmsSinkHandle.SINK_TYPE;

final class JmsSinkConfiguration {

    private static final String JMS_SINK_PROPERTIES_PREFIX = SINK_TYPE + ".";

    private final OutputDestinationConfiguration outputDestinationConfiguration;
    private final OutputMessageConfiguration outputMessageConfiguration;
    private final ConnectionRecoveryConfiguration recoveryConfiguration;
    private final ConnectionPoolConfiguration connectionPoolConfiguration;

    @ProfilerConfigurationPropertiesWrapper(prefix = JMS_SINK_PROPERTIES_PREFIX)
    JmsSinkConfiguration(
            final OutputDestinationConfiguration outputDestinationConfiguration,
            final OutputMessageConfiguration outputMessageConfiguration,
            final ConnectionRecoveryConfiguration recoveryConfiguration,
            final ConnectionPoolConfiguration connectionPoolConfiguration
    ) {
        this.outputDestinationConfiguration = outputDestinationConfiguration;
        this.outputMessageConfiguration = outputMessageConfiguration;
        this.recoveryConfiguration = recoveryConfiguration;
        this.connectionPoolConfiguration = connectionPoolConfiguration;
    }

    OutputDestinationConfiguration outputDestinationConfiguration() {
        return outputDestinationConfiguration;
    }

    OutputMessageConfiguration outputMessageConfiguration() {
        return outputMessageConfiguration;
    }

    ConnectionRecoveryConfiguration recoveryConfiguration() {
        return recoveryConfiguration;
    }

    ConnectionPoolConfiguration connectionPoolConfiguration() {
        return connectionPoolConfiguration;
    }

    @Override
    public String toString() {
        return "JmsSinkConfiguration{"
                + "outputDestinationConfiguration=" + outputDestinationConfiguration
                + ", outputMessageConfiguration=" + outputMessageConfiguration
                + ", recoveryConfiguration=" + recoveryConfiguration
                + ", connectionPoolConfiguration" + connectionPoolConfiguration
                + '}';
    }

    static class OutputDestinationConfiguration {

        private static final String CONNECTION_PROPERTIES_PREFIX = "destination.";

        private static final String CF_JNDI_NAME = "connection_factory_jndi";
        private static final String DESTINATION_JNDI_NAME = "destination_jndi";

        private final String connectionFactoryJndiName;
        private final String destinationJndiName;

        @ProfilerConfigurationPropertiesWrapper(prefix = CONNECTION_PROPERTIES_PREFIX)
        OutputDestinationConfiguration(
                @ProfilerConfigurationProperty(name = CF_JNDI_NAME, required = true) final String connectionFactoryJndiName,
                @ProfilerConfigurationProperty(name = DESTINATION_JNDI_NAME, required = true) final String destinationJndiName
        ) {
            this.connectionFactoryJndiName = connectionFactoryJndiName;
            this.destinationJndiName = destinationJndiName;
        }

        String connectionFactoryJndiName() {
            return connectionFactoryJndiName;
        }

        String destinationJndiName() {
            return destinationJndiName;
        }

        @Override
        public String toString() {
            return "ConnectionConfiguration{"
                    + "connectionFactoryJndiName='" + connectionFactoryJndiName + '\''
                    + ", destinationJndiName=" + destinationJndiName
                    + '}';
        }
    }

    static class OutputMessageConfiguration {

        private static final String OUTPUT_MESSAGE_PROPERTIES_PREFIX = "output-message.";
        
        private static final String MESSAGE_TYPE = "message_type";
        private static final String INCLUDE_MSG_ID = "include_message_id";
        private static final String INCLUDE_MSG_TIMESTAMP = "include_message_timestamp";
        private static final String TTL = "ttl";
        private static final String DELIVERY_DELAY = "delivery_delay";
        private static final String PERSISTENT = "persistent";
        private static final String BODY_MAPPING = "body_mapping";
        private static final String HEADERS_MAPPING = "headers_mapping";

        private final String messageType;
        private final boolean includeMessageId;
        private final boolean includeMessageTimestamp;
        private final long ttlMs;
        private final long deliveryDelayMs;
        private final boolean persistent;
        private final Map<String, String> bodyPropertiesMapping;
        private final Map<String, String> messagePropertiesMapping;

        @ProfilerConfigurationPropertiesWrapper(prefix = OUTPUT_MESSAGE_PROPERTIES_PREFIX)
        OutputMessageConfiguration(
                @ProfilerConfigurationProperty(name = MESSAGE_TYPE, defaultValue = "profiling-data") final String messageType,
                @ProfilerConfigurationProperty(name = INCLUDE_MSG_ID) final boolean includeMessageId,
                @ProfilerConfigurationProperty(name = INCLUDE_MSG_TIMESTAMP) final boolean includeMessageTimestamp,
                @ProfilerConfigurationProperty(name = TTL, defaultValue = "0", parser = MillisTimePropertyParser.class) final long ttlMs,
                @ProfilerConfigurationProperty(name = DELIVERY_DELAY, defaultValue = "0", parser = MillisTimePropertyParser.class) final long deliveryDelayMs,
                @ProfilerConfigurationProperty(name = PERSISTENT) final boolean persistent,
                @ProfilerConfigurationProperty(name = BODY_MAPPING, parser = OutputDataPropertiesMappingConfigurationPropertyParser.class) final Map<String, String> bodyPropertiesMapping,
                @ProfilerConfigurationProperty(name = HEADERS_MAPPING, parser = OutputDataPropertiesMappingConfigurationPropertyParser.class) final Map<String, String> messagePropertiesMapping
        ) {
            this.messageType = messageType;
            this.includeMessageId = includeMessageId;
            this.includeMessageTimestamp = includeMessageTimestamp;
            this.ttlMs = Math.max(ttlMs, 0);
            this.deliveryDelayMs = Math.max(deliveryDelayMs, 0);
            this.persistent = persistent;
            this.bodyPropertiesMapping = bodyPropertiesMapping;
            this.messagePropertiesMapping = messagePropertiesMapping;
        }

        String messageType() {
            return messageType;
        }

        boolean includeMessageId() {
            return includeMessageId;
        }

        boolean includeMessageTimestamp() {
            return includeMessageTimestamp;
        }

        long ttlMs() {
            return ttlMs;
        }

        long deliveryDelayMs() {
            return deliveryDelayMs;
        }

        boolean persistent() {
            return persistent;
        }

        Map<String, String> bodyPropertiesMapping() {
            return bodyPropertiesMapping;
        }

        Map<String, String> messagePropertiesMapping() {
            return messagePropertiesMapping;
        }

        @Override
        public String toString() {
            return "OutputMessageConfiguration{"
                    + "messageType='" + messageType + '\''
                    + ", includeMessageId=" + includeMessageId
                    + ", includeMessageTimestamp=" + includeMessageTimestamp
                    + ", ttlMs=" + ttlMs
                    + ", deliveryDelayMs=" + deliveryDelayMs
                    + ", persistent=" + persistent
                    + ", bodyPropertiesMapping=" + bodyPropertiesMapping
                    + ", messagePropertiesMapping=" + messagePropertiesMapping
                    + '}';
        }
    }
}
