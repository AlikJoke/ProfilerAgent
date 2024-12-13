package ru.joke.profiler.core.output.handlers.async;

public final class AsyncSinkDataFlushingConfiguration {

    private final long flushInterval;
    private final int overflowLimit;
    private final OverflowPolicy overflowPolicy;
    private final int flushingThreadPoolSize;
    private final boolean forceFlushOnExit;

    public AsyncSinkDataFlushingConfiguration(
            final long flushInterval,
            final int flushingThreadPoolSize,
            final int overflowLimit,
            final OverflowPolicy overflowPolicy,
            final boolean forceFlushOnExit) {
        this.flushInterval = flushInterval;
        this.overflowLimit = overflowLimit;
        this.overflowPolicy = overflowPolicy;
        this.flushingThreadPoolSize = flushingThreadPoolSize;
        this.forceFlushOnExit = forceFlushOnExit;
    }

    public long getFlushInterval() {
        return flushInterval;
    }

    public int getOverflowLimit() {
        return overflowLimit;
    }

    public OverflowPolicy getOverflowPolicy() {
        return overflowPolicy;
    }

    public int getFlushingThreadPoolSize() {
        return flushingThreadPoolSize;
    }

    public boolean forceFlushOnExit() {
        return forceFlushOnExit;
    }

    @Override
    public String toString() {
        return "AsyncSinkDataFlushingConfiguration{"
                + "flushInterval=" + flushInterval
                + ", overflowLimit=" + overflowLimit
                + ", overflowPolicy=" + overflowPolicy
                + ", flushingThreadPoolSize=" + flushingThreadPoolSize
                + ", forceFlushOnExit=" + forceFlushOnExit
                + '}';
    }
}
