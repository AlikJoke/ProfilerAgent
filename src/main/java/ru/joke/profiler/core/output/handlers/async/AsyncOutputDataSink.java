package ru.joke.profiler.core.output.handlers.async;

import ru.joke.profiler.core.ProfilerException;
import ru.joke.profiler.core.output.handlers.OutputDataSink;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AsyncOutputDataSink<S, T> implements OutputDataSink<S> {

    private static final Logger logger = Logger.getLogger(OutputDataSink.class.getCanonicalName());

    private static final String FLUSHING_THREAD_NAME_PREFIX = "profiler-flushing-thread-";

    private final OutputDataSink<T> delegateSink;
    private final AsyncSinkDataFlushingConfiguration configuration;
    private final BlockingQueue<Supplier<T>> queue;
    private final ScheduledExecutorService flushExecutor;
    private final Function<S, Supplier<T>> conversionFunc;

    public AsyncOutputDataSink(
            final OutputDataSink<T> delegateSink,
            final AsyncSinkDataFlushingConfiguration configuration,
            final Function<S, Supplier<T>> conversionFunc) {
        this.delegateSink = delegateSink;
        this.configuration = configuration;
        this.queue = new LinkedBlockingQueue<>(configuration.getOverflowLimit());
        final AtomicInteger threadCounter = new AtomicInteger();
        this.flushExecutor = Executors.newScheduledThreadPool(
                configuration.getFlushingThreadPoolSize(),
                r -> {
                    final Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    thread.setName(FLUSHING_THREAD_NAME_PREFIX + threadCounter.incrementAndGet());
                    thread.setUncaughtExceptionHandler((t, e) -> logger.log(Level.SEVERE, "Unable to flush data", e));

                    return thread;
                });
        this.conversionFunc = conversionFunc;
    }

    @Override
    public void init() {
        this.delegateSink.init();
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        this.flushExecutor.scheduleAtFixedRate(
                this::flush,
                this.configuration.getFlushInterval(),
                this.configuration.getFlushInterval(),
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void write(final S outputData) {
        final Supplier<T> dataSupplier = this.conversionFunc.apply(outputData);
        final OverflowPolicy overflowPolicy = this.configuration.getOverflowPolicy();
        while (!this.queue.offer(dataSupplier)) {
            if (overflowPolicy == OverflowPolicy.SYNC) {
                this.delegateSink.write(dataSupplier.get());
                return;
            } else if (overflowPolicy == OverflowPolicy.DISCARD) {
                logger.severe("Async queue is full, data will be discarded");
                return;
            } else if (overflowPolicy == OverflowPolicy.ERROR) {
                throw new ProfilerException(String.format("Unable to offer data to async queue: %s", outputData));
            }
        }
    }

    @Override
    public void close() {
        this.flushExecutor.shutdown();
        if (this.configuration.forceFlushOnExit()) {
            flush();
        }
        this.delegateSink.close();
    }

    private void flush() {
        Supplier<T> data = this.queue.poll();
        while (data != null) {
            this.delegateSink.write(data.get());
            data = this.queue.poll();
        }
    }
}
