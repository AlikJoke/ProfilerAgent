package ru.joke.profiler.output.handlers.kafka;

import ru.joke.profiler.output.handlers.util.recovery.ConnectionRecoveryConfiguration;

import java.util.Collections;
import java.util.Map;

final class KafkaSinkConfiguration {

    private final ProducerConfiguration producerConfiguration;
    private final OutputMessageConfiguration outputMessageConfiguration;
    private final ConnectionRecoveryConfiguration recoveryConfiguration;

    KafkaSinkConfiguration(
            final ProducerConfiguration producerConfiguration,
            final OutputMessageConfiguration outputMessageConfiguration,
            final ConnectionRecoveryConfiguration recoveryConfiguration) {
        this.producerConfiguration = producerConfiguration;
        this.outputMessageConfiguration = outputMessageConfiguration;
        this.recoveryConfiguration = recoveryConfiguration;
    }

    ProducerConfiguration producerConfiguration() {
        return producerConfiguration;
    }

    OutputMessageConfiguration outputMessageConfiguration() {
        return outputMessageConfiguration;
    }

    ConnectionRecoveryConfiguration recoveryConfiguration() {
        return recoveryConfiguration;
    }

    @Override
    public String toString() {
        return "KafkaSinkConfiguration{"
                + "producerConfiguration=" + producerConfiguration
                + ", outputMessageConfiguration=" + outputMessageConfiguration
                + ", recoveryConfiguration=" + recoveryConfiguration
                + '}';
    }

    static class ProducerConfiguration {

        private final Map<String, String> producerProperties;
        private final boolean useCompression;
        private final long waitOnCloseTimeoutMs;
        private final boolean checkClusterOnStart;

        ProducerConfiguration(
                final Map<String, String> producerProperties,
                final boolean useCompression,
                final long waitOnCloseTimeoutMs,
                final boolean checkClusterOnStart) {
            this.producerProperties = producerProperties;
            this.useCompression = useCompression;
            this.waitOnCloseTimeoutMs = waitOnCloseTimeoutMs;
            this.checkClusterOnStart = checkClusterOnStart;
        }

        Map<String, String> producerProperties() {
            return producerProperties;
        }

        boolean useCompression() {
            return useCompression;
        }

        long waitOnCloseTimeoutMs() {
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

    static class OutputMessageConfiguration {

        private final String outputQueue;
        private final String messageType;
        private final String messageTypeHeader;
        private final Map<String, String> propertiesMapping;
        private final Map<String, String> headersMapping;

        OutputMessageConfiguration(
                final String outputQueue,
                final String messageType,
                final String messageTypeHeader,
                final Map<String, String> propertiesMapping,
                final Map<String, String> headersMapping) {
            this.outputQueue = outputQueue;
            this.messageType = messageType;
            this.messageTypeHeader = messageTypeHeader;
            this.headersMapping = Collections.unmodifiableMap(headersMapping);
            this.propertiesMapping = Collections.unmodifiableMap(propertiesMapping);
        }

        String messageTypeHeader() {
            return messageTypeHeader;
        }

        String messageType() {
            return messageType;
        }

        Map<String, String> propertiesMapping() {
            return propertiesMapping;
        }

        Map<String, String> headersMapping() {
            return headersMapping;
        }

        String outputQueue() {
            return outputQueue;
        }

        @Override
        public String toString() {
            return "OutputMessageConfiguration{"
                    + "outputQueue='" + outputQueue + '\''
                    + ", messageType='" + messageType + '\''
                    + ", messageTypeHeader='" + messageTypeHeader + '\''
                    + ", propertiesMapping=" + propertiesMapping
                    + ", headersMapping=" + headersMapping
                    + '}';
        }
    }
}
