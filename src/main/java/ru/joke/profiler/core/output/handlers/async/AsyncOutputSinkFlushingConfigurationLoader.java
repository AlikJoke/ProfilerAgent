package ru.joke.profiler.core.output.handlers.async;

import java.util.Map;

import static ru.joke.profiler.core.configuration.ConfigurationProperties.*;

final class AsyncOutputSinkFlushingConfigurationLoader {

    private static final long DEFAULT_FLUSHING_INTERVAL = 10_000;
    private static final int DEFAULT_FLUSHING_POOL_SIZE = 2;
    private static final int DEFAULT_OVERFLOW_LIMIT = 10_000;

    AsyncSinkDataFlushingConfiguration load(final Map<String, String> sinkProperties) {
        final String flushingIntervalStr = sinkProperties.get(ASYNC_FLUSHING_INTERVAL);
        final long flushingInterval =
                flushingIntervalStr == null || flushingIntervalStr.isEmpty()
                        ? DEFAULT_FLUSHING_INTERVAL
                        : Long.parseLong(flushingIntervalStr);

        final String flushingThreadPoolSizeStr = sinkProperties.get(ASYNC_FLUSHING_POOL_SIZE);
        final int flushingThreadPoolSize =
                flushingThreadPoolSizeStr == null || flushingThreadPoolSizeStr.isEmpty()
                        ? DEFAULT_FLUSHING_POOL_SIZE
                        : Integer.parseInt(flushingThreadPoolSizeStr);

        final String flushingQueueOverflowLimitStr = sinkProperties.get(ASYNC_FLUSHING_QUEUE_OVERFLOW_LIMIT);
        final int flushingQueueOverflowLimit =
                flushingQueueOverflowLimitStr == null || flushingQueueOverflowLimitStr.isEmpty()
                        ? DEFAULT_OVERFLOW_LIMIT
                        : Integer.parseInt(flushingQueueOverflowLimitStr);

        final String flushingOverflowPolicy = sinkProperties.get(ASYNC_FLUSHING_QUEUE_OVERFLOW_POLICY);
        final OverflowPolicy overflowPolicy = OverflowPolicy.parse(flushingOverflowPolicy);

        final boolean forceFlushOnExit = Boolean.parseBoolean(sinkProperties.get(ASYNC_FLUSHING_FORCE_ON_EXIT));
        final String maxFlushBatchSizeStr = sinkProperties.get(ASYNC_FLUSHING_MAX_BATCH_SIZE);
        final int maxFlushBatchSize =
                maxFlushBatchSizeStr == null || maxFlushBatchSizeStr.isEmpty()
                        ? Integer.MAX_VALUE
                        : Integer.parseInt(maxFlushBatchSizeStr);

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
