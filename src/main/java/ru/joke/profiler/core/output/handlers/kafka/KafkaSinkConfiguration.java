package ru.joke.profiler.core.output.handlers.kafka;

import java.util.Collections;
import java.util.Map;

final class KafkaSinkConfiguration {

    private final ProducerConfiguration producerConfiguration;
    private final OutputMessageConfiguration outputMessageConfiguration;
    private final RecoveryConfiguration recoveryConfiguration;

    KafkaSinkConfiguration(
            final ProducerConfiguration producerConfiguration,
            final OutputMessageConfiguration outputMessageConfiguration,
            final RecoveryConfiguration recoveryConfiguration) {
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

    RecoveryConfiguration recoveryConfiguration() {
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

        ProducerConfiguration(
                final Map<String, String> producerProperties,
                final boolean useCompression,
                final long waitOnCloseTimeoutMs) {
            this.producerProperties = producerProperties;
            this.useCompression = useCompression;
            this.waitOnCloseTimeoutMs = waitOnCloseTimeoutMs;
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

        @Override
        public String toString() {
            return "ProducerConfiguration{"
                    + "producerProperties=" + producerProperties
                    + ", useCompression=" + useCompression
                    + ", waitOnCloseTimeoutMs=" + waitOnCloseTimeoutMs
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

    static class RecoveryConfiguration {

        private final long recoveryTimeoutMs;
        private final long maxRetryRecoveryIntervalMs;
        private final ProcessingInRecoveryStatePolicy processingInRecoveryStatePolicy;

        RecoveryConfiguration(
                final long recoveryTimeoutMs,
                final long maxRetryRecoveryIntervalMs,
                final ProcessingInRecoveryStatePolicy processingInRecoveryStatePolicy) {
            this.recoveryTimeoutMs = recoveryTimeoutMs;
            this.maxRetryRecoveryIntervalMs = maxRetryRecoveryIntervalMs;
            this.processingInRecoveryStatePolicy = processingInRecoveryStatePolicy;
        }

        long recoveryTimeoutMs() {
            return recoveryTimeoutMs;
        }

        long maxRetryRecoveryIntervalMs() {
            return maxRetryRecoveryIntervalMs;
        }

        ProcessingInRecoveryStatePolicy processingInRecoveryStatePolicy() {
            return processingInRecoveryStatePolicy;
        }

        @Override
        public String toString() {
            return "RecoveryConfiguration{"
                    + "recoveryTimeoutMs=" + recoveryTimeoutMs
                    + ", maxRetryRecoveryIntervalMs=" + maxRetryRecoveryIntervalMs
                    + ", processingInRecoveryStatePolicy=" + processingInRecoveryStatePolicy
                    + '}';
        }
    }
}
