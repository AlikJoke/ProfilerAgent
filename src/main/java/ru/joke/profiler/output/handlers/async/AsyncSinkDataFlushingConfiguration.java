package ru.joke.profiler.output.handlers.async;

final class AsyncSinkDataFlushingConfiguration {

    private final long flushInterval;
    private final int overflowLimit;
    private final OverflowPolicy overflowPolicy;
    private final int flushingThreadPoolSize;
    private final boolean forceFlushOnExit;
    private final int flushMaxBatchSize;

    AsyncSinkDataFlushingConfiguration(
            final long flushInterval,
            final int flushingThreadPoolSize,
            final int overflowLimit,
            final OverflowPolicy overflowPolicy,
            final boolean forceFlushOnExit,
            final int flushMaxBatchSize) {
        this.flushInterval = flushInterval;
        this.overflowLimit = overflowLimit;
        this.overflowPolicy = overflowPolicy;
        this.flushingThreadPoolSize = flushingThreadPoolSize;
        this.forceFlushOnExit = forceFlushOnExit;
        this.flushMaxBatchSize = flushMaxBatchSize;
    }

    long flushInterval() {
        return flushInterval;
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
                + "flushInterval=" + flushInterval
                + ", overflowLimit=" + overflowLimit
                + ", overflowPolicy=" + overflowPolicy
                + ", flushingThreadPoolSize=" + flushingThreadPoolSize
                + ", forceFlushOnExit=" + forceFlushOnExit
                + ", flushMaxBatchSize=" + flushMaxBatchSize
                + '}';
    }
}
