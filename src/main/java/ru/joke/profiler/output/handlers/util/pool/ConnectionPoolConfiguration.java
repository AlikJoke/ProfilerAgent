package ru.joke.profiler.output.handlers.util.pool;

import ru.joke.profiler.configuration.InvalidConfigurationException;

public final class ConnectionPoolConfiguration {

    private final boolean enablePooling;
    private final int maxPoolSize;
    private final int initialPoolSize;
    private final long keepAliveIdleTime;
    private final long maxConnectionWaitTime;
    private final ConnectionUnavailabilityPolicy connectionUnavailabilityPolicy;

    public ConnectionPoolConfiguration(
            final boolean enablePooling,
            final int maxPoolSize,
            final int initialPoolSize,
            final long keepAliveIdleTime,
            final long maxConnectionWaitTime,
            final ConnectionUnavailabilityPolicy connectionUnavailabilityPolicy) {
        this.enablePooling = enablePooling;
        this.maxPoolSize = maxPoolSize;
        this.initialPoolSize = initialPoolSize;
        this.keepAliveIdleTime = keepAliveIdleTime;
        this.maxConnectionWaitTime = maxConnectionWaitTime;
        this.connectionUnavailabilityPolicy = connectionUnavailabilityPolicy;
    }

    public boolean enablePooling() {
        return enablePooling;
    }

    public int maxPoolSize() {
        return maxPoolSize;
    }

    public int initialPoolSize() {
        return initialPoolSize;
    }

    public long keepAliveIdleTime() {
        return keepAliveIdleTime;
    }

    public long maxConnectionWaitTime() {
        return maxConnectionWaitTime;
    }

    public ConnectionUnavailabilityPolicy connectionUnavailabilityPolicy() {
        return connectionUnavailabilityPolicy;
    }

    @Override
    public String toString() {
        return "ConnectionPoolConfiguration{"
                + "enablePooling=" + enablePooling
                + ", maxPoolSize=" + maxPoolSize
                + ", initialPoolSize=" + initialPoolSize
                + ", keepAliveIdleTime=" + keepAliveIdleTime
                + ", maxConnectionWaitTime=" + maxConnectionWaitTime
                + ", connectionUnavailabilityPolicy=" + connectionUnavailabilityPolicy
                + '}';
    }

    public enum ConnectionUnavailabilityPolicy {

        SKIP,

        ERROR;

        public static ConnectionUnavailabilityPolicy parse(final String alias) {
            for (final ConnectionUnavailabilityPolicy policy : values()) {
                if (policy.name().equalsIgnoreCase(alias)) {
                    return policy;
                }
            }

            if (alias == null || alias.isEmpty()) {
                return ERROR;
            }

            throw new InvalidConfigurationException("Unknown type of policy: " + alias);
        }
    }
}
