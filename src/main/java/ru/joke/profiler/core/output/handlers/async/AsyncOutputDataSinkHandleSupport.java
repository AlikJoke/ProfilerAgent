package ru.joke.profiler.core.output.handlers.async;

import ru.joke.profiler.core.output.handlers.OutputData;
import ru.joke.profiler.core.output.handlers.OutputDataSink;
import ru.joke.profiler.core.output.handlers.OutputDataSinkHandle;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static ru.joke.profiler.core.configuration.ConfigurationProperties.*;

public abstract class AsyncOutputDataSinkHandleSupport<T> implements OutputDataSinkHandle {

    private static final long DEFAULT_FLUSHING_INTERVAL = 10_000;
    private static final int DEFAULT_FLUSHING_POOL_SIZE = 2;
    private static final int DEFAULT_OVERFLOW_LIMIT = 10_000;

    @Override
    public final OutputDataSink<OutputData> create(final Map<String, String> properties) throws Exception {
        final boolean asyncFlushingEnabled = Boolean.parseBoolean(properties.get(ASYNC_FLUSHING_ENABLED));
        final Map<String, Object> creationContext = buildCreationContext(properties);
        return asyncFlushingEnabled
                ? createAsyncOutputSync(properties, creationContext)
                : createSyncOutputSink(properties, creationContext);
    }

    protected Map<String, Object> buildCreationContext(final Map<String, String> properties) {
        return Collections.emptyMap();
    }

    protected abstract Function<OutputData, Supplier<T>> conversionFunction(
            final Map<String, String> properties,
            final Map<String, Object> context
    );

    protected abstract OutputDataSink<T> createTerminalOutputSink(
            final Map<String, String> properties,
            final Map<String, Object> context
    ) throws Exception;

    protected abstract OutputDataSink<OutputData> createSyncOutputSink(
            final Map<String, String> properties,
            final Map<String, Object> context
    ) throws Exception;

    protected OutputDataSink<OutputData> createAsyncOutputSync(
            final Map<String, String> properties,
            final Map<String, Object> context
    ) throws Exception {
        final OutputDataSink<T> sink = createTerminalOutputSink(properties, context);
        final AsyncSinkDataFlushingConfiguration configuration = composeConfiguration(properties);
        return new AsyncOutputDataSink<>(sink, configuration, conversionFunction(properties, context));
    }

    private AsyncSinkDataFlushingConfiguration composeConfiguration(final Map<String, String> properties) {
        final String flushingIntervalStr = properties.get(ASYNC_FLUSHING_INTERVAL);
        final long flushingInterval =
                flushingIntervalStr == null || flushingIntervalStr.isEmpty()
                        ? DEFAULT_FLUSHING_INTERVAL
                        : Long.parseLong(flushingIntervalStr);

        final String flushingThreadPoolSizeStr = properties.get(ASYNC_FLUSHING_POOL_SIZE);
        final int flushingThreadPoolSize =
                flushingThreadPoolSizeStr == null || flushingThreadPoolSizeStr.isEmpty()
                        ? DEFAULT_FLUSHING_POOL_SIZE
                        : Integer.parseInt(flushingThreadPoolSizeStr);

        final String flushingQueueOverflowLimitStr = properties.get(ASYNC_FLUSHING_QUEUE_OVERFLOW_LIMIT);
        final int flushingQueueOverflowLimit =
                flushingQueueOverflowLimitStr == null || flushingQueueOverflowLimitStr.isEmpty()
                        ? DEFAULT_OVERFLOW_LIMIT
                        : Integer.parseInt(flushingQueueOverflowLimitStr);

        final String flushingOverflowPolicy = properties.get(ASYNC_FLUSHING_QUEUE_OVERFLOW_POLICY);
        final OverflowPolicy overflowPolicy = OverflowPolicy.parse(flushingOverflowPolicy);

        final boolean forceFlushOnExit = Boolean.parseBoolean(properties.get(ASYNC_FLUSHING_FORCE_ON_EXIT));

        return new AsyncSinkDataFlushingConfiguration(
                flushingInterval,
                flushingThreadPoolSize,
                flushingQueueOverflowLimit,
                overflowPolicy,
                forceFlushOnExit
        );
    }
}
