package ru.joke.profiler.output.handlers.util.recovery;

public final class ConnectionRecoveryConfiguration {

    private final long recoveryTimeoutMs;
    private final long maxRetryRecoveryIntervalMs;
    private final ProcessingInRecoveryStatePolicy processingInRecoveryStatePolicy;

    public ConnectionRecoveryConfiguration(
            final long recoveryTimeoutMs,
            final long maxRetryRecoveryIntervalMs,
            final ProcessingInRecoveryStatePolicy processingInRecoveryStatePolicy) {
        this.recoveryTimeoutMs = recoveryTimeoutMs;
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
}
