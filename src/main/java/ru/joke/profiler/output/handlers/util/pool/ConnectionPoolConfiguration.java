package ru.joke.profiler.output.handlers.util.pool;

import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.meta.ProfilerDefaultEnumProperty;

import static ru.joke.profiler.configuration.ConfigurationProperties.*;

public final class ConnectionPoolConfiguration {

    private final boolean enablePooling;
    private final int maxPoolSize;
    private final int initialPoolSize;
    private final long keepAliveIdleTime;
    private final long maxConnectionWaitTime;
    private final ConnectionUnavailabilityPolicy connectionUnavailabilityPolicy;

    @ProfilerConfigurationPropertiesWrapper(prefix = SINK_CONNECTION_POOL_PROPERTIES_PREFIX)
    public ConnectionPoolConfiguration(
            @ProfilerConfigurationProperty(name = STATIC_SINK_CONNECTION_POOL_ENABLED, defaultValue = "true") final boolean enablePooling,
            @ProfilerConfigurationProperty(name = STATIC_SINK_CONNECTION_POOL_MAX_POOL, defaultValue = "32") final int maxPoolSize,
            @ProfilerConfigurationProperty(name = STATIC_SINK_CONNECTION_POOL_INIT_POOL, defaultValue = "4") final int initialPoolSize,
            @ProfilerConfigurationProperty(name = STATIC_SINK_CONNECTION_POOL_KEEP_ALIVE_IDLE, defaultValue = "120000") final long keepAliveIdleTime,
            @ProfilerConfigurationProperty(name = STATIC_SINK_CONNECTION_POOL_MAX_WAIT, defaultValue = "3000") final long maxConnectionWaitTime,
            @ProfilerConfigurationProperty(name = STATIC_SINK_CONNECTION_POOL_CONN_UNAVAILABILITY_POLICY) final ConnectionUnavailabilityPolicy connectionUnavailabilityPolicy
    ) {
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

        @SuppressWarnings("unused")
        SKIP,

        @ProfilerDefaultEnumProperty
        ERROR
    }
}
