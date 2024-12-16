package ru.joke.profiler.core.output.handlers.async;

import java.util.Map;

import static ru.joke.profiler.core.configuration.ConfigurationProperties.*;

final class AsyncOutputSinkFlushingConfigurationLoader {

    private static final long DEFAULT_FLUSHING_INTERVAL = 10_000;
    private static final int DEFAULT_FLUSHING_POOL_SIZE = 2;
    private static final int DEFAULT_OVERFLOW_LIMIT = 10_000;

    AsyncSinkDataFlushingConfiguration load(final Map<String, String> sinkProperties) {
        final long flushingInterval = parseLongProperty(sinkProperties, ASYNC_FLUSHING_INTERVAL, DEFAULT_FLUSHING_INTERVAL);
        final int flushingThreadPoolSize = parseIntProperty(sinkProperties, ASYNC_FLUSHING_POOL_SIZE, DEFAULT_FLUSHING_POOL_SIZE);
        final int flushingQueueOverflowLimit = parseIntProperty(sinkProperties, ASYNC_FLUSHING_QUEUE_OVERFLOW_LIMIT, DEFAULT_OVERFLOW_LIMIT);

        final String flushingOverflowPolicy = sinkProperties.get(ASYNC_FLUSHING_QUEUE_OVERFLOW_POLICY);
        final OverflowPolicy overflowPolicy = OverflowPolicy.parse(flushingOverflowPolicy);

        final boolean forceFlushOnExit = parseBooleanProperty(sinkProperties, ASYNC_FLUSHING_FORCE_ON_EXIT);
        final int maxFlushBatchSize = parseIntProperty(sinkProperties, ASYNC_FLUSHING_MAX_BATCH_SIZE, Integer.MAX_VALUE);

        return new AsyncSinkDataFlushingConfiguration(
                flushingInterval,
                flushingThreadPoolSize,
                flushingQueueOverflowLimit,
                overflowPolicy,
                forceFlushOnExit,
                maxFlushBatchSize
        );
    }
}
