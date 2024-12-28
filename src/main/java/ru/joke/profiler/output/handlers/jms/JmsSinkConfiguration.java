package ru.joke.profiler.output.handlers.jms;

import ru.joke.profiler.output.handlers.util.pool.ConnectionPoolConfiguration;
import ru.joke.profiler.output.handlers.util.recovery.ConnectionRecoveryConfiguration;

import java.util.Map;

final class JmsSinkConfiguration {

    private final ConnectionConfiguration connectionConfiguration;
    private final OutputMessageConfiguration outputMessageConfiguration;
    private final ConnectionRecoveryConfiguration recoveryConfiguration;

    JmsSinkConfiguration(
            final ConnectionConfiguration connectionConfiguration,
            final OutputMessageConfiguration outputMessageConfiguration,
            final ConnectionRecoveryConfiguration recoveryConfiguration
    ) {
        this.connectionConfiguration = connectionConfiguration;
        this.outputMessageConfiguration = outputMessageConfiguration;
        this.recoveryConfiguration = recoveryConfiguration;
    }

    public ConnectionConfiguration connectionConfiguration() {
        return connectionConfiguration;
    }

    public OutputMessageConfiguration outputMessageConfiguration() {
        return outputMessageConfiguration;
    }

    public ConnectionRecoveryConfiguration recoveryConfiguration() {
        return recoveryConfiguration;
    }

    @Override
    public String toString() {
        return "JmsSinkConfiguration{"
                + "connectionConfiguration=" + connectionConfiguration
                + ", outputMessageConfiguration=" + outputMessageConfiguration
                + ", recoveryConfiguration=" + recoveryConfiguration
                + '}';
    }

    static class ConnectionConfiguration {

        private final String connectionFactoryJndiName;
        private final ConnectionPoolConfiguration connectionPoolConfiguration;

        public ConnectionConfiguration(
                final String connectionFactoryJndiName,
                final ConnectionPoolConfiguration connectionPoolConfiguration
        ) {
            this.connectionFactoryJndiName = connectionFactoryJndiName;
            this.connectionPoolConfiguration = connectionPoolConfiguration;
        }

        public String connectionFactoryJndiName() {
            return connectionFactoryJndiName;
        }

        public ConnectionPoolConfiguration connectionPoolConfiguration() {
            return connectionPoolConfiguration;
        }

        @Override
        public String toString() {
            return "ConnectionConfiguration{"
                    + "connectionFactoryJndiName='" + connectionFactoryJndiName + '\''
                    + ", connectionPoolConfiguration=" + connectionPoolConfiguration
                    + '}';
        }
    }

    static class OutputMessageConfiguration {

        private final String targetEndpointJndiName;
        private final String messageType;
        private final boolean includeMessageId;
        private final boolean includeMessageTimestamp;
        private final long ttlMs;
        private final long deliveryDelayMs;
        private final boolean persistent;
        private final Map<String, String> bodyPropertiesMapping;
        private final Map<String, String> messagePropertiesMapping;

        OutputMessageConfiguration(
                final String targetEndpointJndiName,
                final String messageType,
                final boolean includeMessageId,
                final boolean includeMessageTimestamp,
                final long ttlMs,
                final long deliveryDelayMs,
                final boolean persistent,
                final Map<String, String> bodyPropertiesMapping,
                final Map<String, String> messagePropertiesMapping
        ) {
            this.targetEndpointJndiName = targetEndpointJndiName;
            this.messageType = messageType;
            this.includeMessageId = includeMessageId;
            this.includeMessageTimestamp = includeMessageTimestamp;
            this.ttlMs = ttlMs;
            this.deliveryDelayMs = deliveryDelayMs;
            this.persistent = persistent;
            this.bodyPropertiesMapping = bodyPropertiesMapping;
            this.messagePropertiesMapping = messagePropertiesMapping;
        }

        public String targetEndpointJndiName() {
            return targetEndpointJndiName;
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
                    + "targetEndpointJndiName='" + targetEndpointJndiName + '\''
                    + ", messageType='" + messageType + '\''
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
