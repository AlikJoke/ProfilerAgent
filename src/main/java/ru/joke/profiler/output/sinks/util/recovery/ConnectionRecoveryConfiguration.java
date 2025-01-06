package ru.joke.profiler.output.sinks.util.recovery;

import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.meta.ProfilerDefaultEnumProperty;
import ru.joke.profiler.configuration.util.MillisTimePropertyParser;

public final class ConnectionRecoveryConfiguration {

    private static final String RECOVERY_CONFIGURATION_PREFIX = "connection-recovery.";

    private static final String TIMEOUT = "timeout";
    private static final String MAX_RETRY_INTERVAL = "max_retry_interval";
    private static final String PROCESSING_POLICY = "processing_policy";

    private final long recoveryTimeoutMs;
    private final long maxRetryRecoveryIntervalMs;
    private final ProcessingInRecoveryStatePolicy processingInRecoveryStatePolicy;

    @ProfilerConfigurationPropertiesWrapper(prefix = RECOVERY_CONFIGURATION_PREFIX)
    public ConnectionRecoveryConfiguration(
            @ProfilerConfigurationProperty(name = TIMEOUT, defaultValue = "-1", parser = MillisTimePropertyParser.class) final long recoveryTimeoutMs,
            @ProfilerConfigurationProperty(name = MAX_RETRY_INTERVAL, defaultValue = "30s", parser = MillisTimePropertyParser.class) final long maxRetryRecoveryIntervalMs,
            @ProfilerConfigurationProperty(name = PROCESSING_POLICY) final ProcessingInRecoveryStatePolicy processingInRecoveryStatePolicy
    ) {
        this.recoveryTimeoutMs = recoveryTimeoutMs == -1 ? Long.MAX_VALUE : recoveryTimeoutMs;
        this.maxRetryRecoveryIntervalMs = maxRetryRecoveryIntervalMs;
        this.processingInRecoveryStatePolicy = processingInRecoveryStatePolicy;
    }

    public long recoveryTimeoutMs() {
        return recoveryTimeoutMs;
    }

    public long maxRetryRecoveryIntervalMs() {
        return maxRetryRecoveryIntervalMs;
    }

    public ProcessingInRecoveryStatePolicy processingInRecoveryStatePolicy() {
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

    public enum ProcessingInRecoveryStatePolicy {

        @ProfilerDefaultEnumProperty
        SKIP,

        WAIT,

        ERROR
    }
}
