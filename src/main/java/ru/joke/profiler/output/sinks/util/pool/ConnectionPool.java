package ru.joke.profiler.output.sinks.util.pool;

public interface ConnectionPool<T extends PooledConnection> extends AutoCloseable {

    void init();

    void release(T connection);

    T get();

    @Override
    void close();
}
