package ru.joke.profiler.output.sinks.async;

import ru.joke.profiler.output.sinks.OutputDataSink;
import ru.joke.profiler.output.sinks.ProfilerOutputSinkException;
import ru.joke.profiler.output.sinks.util.ConcurrentLinkedBlockingQueue;
import ru.joke.profiler.util.ProfilerThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

final class AsyncOutputDataSink<S, T> extends OutputDataSink<S> {

    private static final Logger logger = Logger.getLogger(AsyncOutputDataSink.class.getCanonicalName());

    private static final String FLUSHING_THREAD_NAME_PREFIX_TEMPLATE = "profiler-%s-flushing-thread-";

    private final OutputDataSink<T> delegateSink;
    private final AsyncSinkDataFlushingConfiguration configuration;
    private final ConcurrentLinkedBlockingQueue<Supplier<T>> queue;
    private final ScheduledExecutorService flushExecutor;
    private final Function<S, Supplier<T>> conversionFunc;

    AsyncOutputDataSink(
            final OutputDataSink<T> delegateSink,
            final AsyncSinkDataFlushingConfiguration configuration,
            final Function<S, Supplier<T>> conversionFunc,
            final String sinkType
    ) {
        this.delegateSink = checkNotNull(delegateSink, "delegateSink");
        this.configuration = checkNotNull(configuration, "configuration");
        this.queue = new ConcurrentLinkedBlockingQueue<>(configuration.overflowLimit());
        final String threadNamePrefix = String.format(FLUSHING_THREAD_NAME_PREFIX_TEMPLATE, sinkType);
        this.flushExecutor = Executors.newScheduledThreadPool(
                configuration.flushingThreadPoolSize(),
                new ProfilerThreadFactory(threadNamePrefix, true)
        );
        this.conversionFunc = checkNotNull(conversionFunc, "conversionFunc");
    }

    @Override
    public void init() {
        logger.info(String.format("Async sink wrapper for %s will be initialized", this.delegateSink));

        this.delegateSink.init();
        this.flushExecutor.scheduleAtFixedRate(
                this::flush,
                this.configuration.flushIntervalMs(),
                this.configuration.flushIntervalMs(),
                TimeUnit.MILLISECONDS
        );

        logger.info(String.format("Async sink wrapper for %s initialized with configuration: %s", this.delegateSink, this.configuration));
    }

    @Override
    public void write(final S outputData) {
        final Supplier<T> dataSupplier = this.conversionFunc.apply(outputData);
        final AsyncSinkDataFlushingConfiguration.OverflowPolicy overflowPolicy = this.configuration.overflowPolicy();
        while (!this.queue.offer(dataSupplier)) {
            switch (overflowPolicy) {
                case SYNC:
                    logger.severe("Async queue is full, data will be written sync");
                    this.delegateSink.write(dataSupplier.get());
                    return;
                case DISCARD:
                    logger.severe("Async queue is full, data will be discarded");
                    return;
                case ERROR:
                    throw new ProfilerOutputSinkException(String.format("Unable to offer data to async queue: %s", outputData));
            }
        }
    }

    @Override
    public void close() {
        logger.info(String.format("Async sink wrapper for %s will be closed", this.delegateSink));

        this.flushExecutor.shutdownNow();
        if (this.configuration.forceFlushOnExit()) {
            flush();
        }
        this.delegateSink.close();

        logger.info(String.format("Async sink wrapper for %s closed", this.delegateSink));
    }

    private void flush() {
        Supplier<T> data = this.queue.poll();
        final List<T> dataItems = new ArrayList<>();
        while (data != null) {
            dataItems.add(data.get());

            if (this.configuration.flushMaxBatchSize() == dataItems.size()) {
                this.delegateSink.write(dataItems);
                dataItems.clear();
            }

            data = this.queue.poll();
        }

        if (!dataItems.isEmpty()) {
            this.delegateSink.write(dataItems);
        }
    }
}
