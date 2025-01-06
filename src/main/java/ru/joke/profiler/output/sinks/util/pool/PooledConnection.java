package ru.joke.profiler.output.sinks.util.pool;

public interface PooledConnection extends AutoCloseable {
    
    long lastUsedTimestamp();
    
    boolean init();

    void onRelease();

    @Override
    void close();
}
