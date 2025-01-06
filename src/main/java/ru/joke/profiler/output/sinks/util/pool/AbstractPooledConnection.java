package ru.joke.profiler.output.sinks.util.pool;

public abstract class AbstractPooledConnection implements PooledConnection {

    private volatile long lastUsedTimestamp;

    @Override
    public long lastUsedTimestamp() {
        return this.lastUsedTimestamp;
    }

    @Override
    public boolean init() {
        this.lastUsedTimestamp = System.currentTimeMillis();
        return true;
    }

    @Override
    public void onRelease() {
        this.lastUsedTimestamp = System.currentTimeMillis();
    }
}
