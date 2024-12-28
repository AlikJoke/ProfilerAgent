package ru.joke.profiler.output.handlers.async;

import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.meta.ProfilerDefaultEnumProperty;
import ru.joke.profiler.configuration.util.MillisTimePropertyParser;

final class AsyncSinkDataFlushingConfiguration {

    private static final String ASYNC_FLUSHING_CONFIGURATION = "async-flushing.";

    private static final String ASYNC_FLUSHING_ENABLED = "enabled";
    private static final String FLUSHING_INTERVAL = "flushing_interval";
    private static final String FLUSHING_POOL_SIZE = "flushing_pool_size";
    private static final String FLUSHING_QUEUE_OVERFLOW_LIMIT = "flushing_queue_overflow_limit";
    private static final String FLUSHING_QUEUE_OVERFLOW_POLICY = "flushing_queue_overflow_policy";
    private static final String FLUSHING_FORCE_ON_EXIT = "force_flush_on_exit";
    private static final String FLUSHING_MAX_BATCH_SIZE = "flushing_max_batch_size";

    private final boolean asyncFlushingEnabled;
    private final long flushIntervalMs;
    private final int overflowLimit;
    private final OverflowPolicy overflowPolicy;
    private final int flushingThreadPoolSize;
    private final boolean forceFlushOnExit;
    private final int flushMaxBatchSize;

    @ProfilerConfigurationPropertiesWrapper(prefix = ASYNC_FLUSHING_CONFIGURATION)
    AsyncSinkDataFlushingConfiguration(
            @ProfilerConfigurationProperty(name = ASYNC_FLUSHING_ENABLED) final boolean asyncFlushingEnabled,
            @ProfilerConfigurationProperty(name = FLUSHING_INTERVAL, defaultValue = "10s", parser = MillisTimePropertyParser.class) final long flushIntervalMs,
            @ProfilerConfigurationProperty(name = FLUSHING_POOL_SIZE, defaultValue = "2") final int flushingThreadPoolSize,
            @ProfilerConfigurationProperty(name = FLUSHING_QUEUE_OVERFLOW_LIMIT, defaultValue = "10000") final int overflowLimit,
            @ProfilerConfigurationProperty(name = FLUSHING_QUEUE_OVERFLOW_POLICY) final OverflowPolicy overflowPolicy,
            @ProfilerConfigurationProperty(name = FLUSHING_FORCE_ON_EXIT) final boolean forceFlushOnExit,
            @ProfilerConfigurationProperty(name = FLUSHING_MAX_BATCH_SIZE, defaultValue = "-1") final int flushMaxBatchSize
    ) {
        this.asyncFlushingEnabled = asyncFlushingEnabled;
        this.flushIntervalMs = flushIntervalMs;
        this.overflowLimit = overflowLimit == -1 ? Integer.MAX_VALUE : overflowLimit;
        this.overflowPolicy = overflowPolicy;
        this.flushingThreadPoolSize = flushingThreadPoolSize;
        this.forceFlushOnExit = forceFlushOnExit;
        this.flushMaxBatchSize = flushMaxBatchSize == -1 ? Integer.MAX_VALUE : flushMaxBatchSize;
    }

    boolean asyncFlushingEnabled() {
        return asyncFlushingEnabled;
    }

    long flushIntervalMs() {
        return flushIntervalMs;
    }

    int overflowLimit() {
        return overflowLimit;
    }

    OverflowPolicy overflowPolicy() {
        return overflowPolicy;
    }

    int flushingThreadPoolSize() {
        return flushingThreadPoolSize;
    }

    boolean forceFlushOnExit() {
        return forceFlushOnExit;
    }

    int flushMaxBatchSize() {
        return flushMaxBatchSize;
    }

    @Override
    public String toString() {
        return "AsyncSinkDataFlushingConfiguration{"
                + "asyncFlushingEnabled=" + asyncFlushingEnabled
                + ", flushIntervalMs=" + flushIntervalMs
                + ", overflowLimit=" + overflowLimit
                + ", overflowPolicy=" + overflowPolicy
                + ", flushingThreadPoolSize=" + flushingThreadPoolSize
                + ", forceFlushOnExit=" + forceFlushOnExit
                + ", flushMaxBatchSize=" + flushMaxBatchSize
                + '}';
    }

    enum OverflowPolicy {

        @ProfilerDefaultEnumProperty
        SYNC,

        DISCARD,

        WAIT,

        ERROR
    }
}
