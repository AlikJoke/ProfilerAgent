package ru.joke.profiler.output.handlers.util.pool;

import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.meta.ProfilerDefaultEnumProperty;
import ru.joke.profiler.configuration.util.MillisTimePropertyParser;

public final class ConnectionPoolConfiguration {

    private static final String CONNECTION_POOL_PROPERTIES_PREFIX = "connection-pool.";

    private static final String ENABLED = "use_connection_pool";
    private static final String MAX_POOL_SIZE = "max_pool_size";
    private static final String INIT_POOL_SIZE = "initial_pool_size";
    private static final String KEEP_ALIVE_IDLE_CONNECTIONS = "keep_alive_idle_time";
    private static final String MAX_WAIT_CONNECTION = "max_connection_wait_time";
    private static final String CONN_UNAVAILABILITY_POLICY = "connection_unavailability_policy";
    
    private final boolean enablePooling;
    private final int maxPoolSize;
    private final int initialPoolSize;
    private final long keepAliveIdleMs;
    private final long maxConnectionWaitMs;
    private final ConnectionUnavailabilityPolicy connectionUnavailabilityPolicy;

    @ProfilerConfigurationPropertiesWrapper(prefix = CONNECTION_POOL_PROPERTIES_PREFIX)
    public ConnectionPoolConfiguration(
            @ProfilerConfigurationProperty(name = ENABLED, defaultValue = "true") final boolean enablePooling,
            @ProfilerConfigurationProperty(name = MAX_POOL_SIZE, defaultValue = "32") final int maxPoolSize,
            @ProfilerConfigurationProperty(name = INIT_POOL_SIZE, defaultValue = "4") final int initialPoolSize,
            @ProfilerConfigurationProperty(name = KEEP_ALIVE_IDLE_CONNECTIONS, defaultValue = "2m", parser = MillisTimePropertyParser.class) final long keepAliveIdleMs,
            @ProfilerConfigurationProperty(name = MAX_WAIT_CONNECTION, defaultValue = "3s", parser = MillisTimePropertyParser.class) final long maxConnectionWaitMs,
            @ProfilerConfigurationProperty(name = CONN_UNAVAILABILITY_POLICY) final ConnectionUnavailabilityPolicy connectionUnavailabilityPolicy
    ) {
        this.enablePooling = enablePooling;
        this.maxPoolSize = maxPoolSize;
        this.initialPoolSize = initialPoolSize;
        this.keepAliveIdleMs = keepAliveIdleMs;
        this.maxConnectionWaitMs = maxConnectionWaitMs;
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

    public long keepAliveIdleMs() {
        return keepAliveIdleMs;
    }

    public long maxConnectionWaitMs() {
        return maxConnectionWaitMs;
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
                + ", keepAliveIdleMs=" + keepAliveIdleMs
                + ", maxConnectionWaitMs=" + maxConnectionWaitMs
                + ", connectionUnavailabilityPolicy=" + connectionUnavailabilityPolicy
                + '}';
    }

    public enum ConnectionUnavailabilityPolicy {

        SKIP,

        @ProfilerDefaultEnumProperty
        ERROR
    }
}
