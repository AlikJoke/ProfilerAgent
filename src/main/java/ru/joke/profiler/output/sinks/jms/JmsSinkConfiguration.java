package ru.joke.profiler.output.sinks.jms;

import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.util.MillisTimePropertyParser;
import ru.joke.profiler.output.sinks.async.AsyncOutputDataSinkConfigurationSupport;
import ru.joke.profiler.output.sinks.async.AsyncSinkDataFlushingConfiguration;
import ru.joke.profiler.output.sinks.util.parsers.OutputDataPropertiesMappingConfigurationPropertyParser;
import ru.joke.profiler.output.sinks.util.pool.ConnectionPoolConfiguration;
import ru.joke.profiler.output.sinks.util.recovery.ConnectionRecoveryConfiguration;

import java.util.Map;

import static ru.joke.profiler.output.sinks.jms.OutputDataJmsSinkHandle.SINK_TYPE;

public final class JmsSinkConfiguration extends AsyncOutputDataSinkConfigurationSupport {

    private static final String JMS_SINK_PROPERTIES_PREFIX = SINK_TYPE + ".";

    private final OutputDestinationConfiguration outputDestinationConfiguration;
    private final OutputMessageConfiguration outputMessageConfiguration;
    private final ConnectionRecoveryConfiguration recoveryConfiguration;
    private final ConnectionPoolConfiguration connectionPoolConfiguration;

    @ProfilerConfigurationPropertiesWrapper(prefix = JMS_SINK_PROPERTIES_PREFIX)
    public JmsSinkConfiguration(
            final OutputDestinationConfiguration outputDestinationConfiguration,
            final OutputMessageConfiguration outputMessageConfiguration,
            final ConnectionRecoveryConfiguration recoveryConfiguration,
            final ConnectionPoolConfiguration connectionPoolConfiguration,
            final AsyncSinkDataFlushingConfiguration asyncFlushingConfiguration
    ) {
        super(asyncFlushingConfiguration);
        this.outputDestinationConfiguration = outputDestinationConfiguration;
        this.outputMessageConfiguration = outputMessageConfiguration;
        this.recoveryConfiguration = recoveryConfiguration;
        this.connectionPoolConfiguration = connectionPoolConfiguration;
    }

    public OutputDestinationConfiguration outputDestinationConfiguration() {
        return outputDestinationConfiguration;
    }

    public OutputMessageConfiguration outputMessageConfiguration() {
        return outputMessageConfiguration;
    }

    public ConnectionRecoveryConfiguration recoveryConfiguration() {
        return recoveryConfiguration;
    }

    public ConnectionPoolConfiguration connectionPoolConfiguration() {
        return connectionPoolConfiguration;
    }

    @Override
    public String toString() {
        return "JmsSinkConfiguration{"
                + "outputDestinationConfiguration=" + outputDestinationConfiguration
                + ", outputMessageConfiguration=" + outputMessageConfiguration
                + ", recoveryConfiguration=" + recoveryConfiguration
                + ", connectionPoolConfiguration" + connectionPoolConfiguration
                + ", asyncFlushingConfiguration=" + asyncFlushingConfiguration
                + '}';
    }

    public static class OutputDestinationConfiguration {

        private static final String CONNECTION_PROPERTIES_PREFIX = "destination.";

        private static final String CF_JNDI_NAME = "connection_factory_jndi";
        private static final String DESTINATION_JNDI_NAME = "destination_jndi";

        private final String connectionFactoryJndiName;
        private final String destinationJndiName;

        @ProfilerConfigurationPropertiesWrapper(prefix = CONNECTION_PROPERTIES_PREFIX)
        public OutputDestinationConfiguration(
                @ProfilerConfigurationProperty(name = CF_JNDI_NAME, required = true) final String connectionFactoryJndiName,
                @ProfilerConfigurationProperty(name = DESTINATION_JNDI_NAME, required = true) final String destinationJndiName
        ) {
            this.connectionFactoryJndiName = connectionFactoryJndiName;
            this.destinationJndiName = destinationJndiName;
        }

        public String connectionFactoryJndiName() {
            return connectionFactoryJndiName;
        }

        public String destinationJndiName() {
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

    public static class OutputMessageConfiguration {

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
        public OutputMessageConfiguration(
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

        public String messageType() {
            return messageType;
        }

        public boolean includeMessageId() {
            return includeMessageId;
        }

        public boolean includeMessageTimestamp() {
            return includeMessageTimestamp;
        }

        public long ttlMs() {
            return ttlMs;
        }

        public long deliveryDelayMs() {
            return deliveryDelayMs;
        }

        public boolean persistent() {
            return persistent;
        }

        public Map<String, String> bodyPropertiesMapping() {
            return bodyPropertiesMapping;
        }

        public Map<String, String> messagePropertiesMapping() {
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
