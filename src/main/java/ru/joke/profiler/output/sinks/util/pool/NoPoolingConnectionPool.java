package ru.joke.profiler.output.sinks.util.pool;

import ru.joke.profiler.output.sinks.ProfilerOutputSinkException;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

public final class NoPoolingConnectionPool<T extends PooledConnection> implements ConnectionPool<T> {

    private final ConnectionFactory<T> connectionFactory;

    public NoPoolingConnectionPool(final ConnectionFactory<T> connectionFactory) {
        this.connectionFactory = checkNotNull(connectionFactory, "connectionFactory");
    }

    @Override
    public void init() {
    }

    @Override
    public void release(final T connection) {
        connection.close();
    }

    @Override
    public T get() {
        final T connectionWrapper = this.connectionFactory.create();
        if (!connectionWrapper.init()) {
            throw new ProfilerOutputSinkException("Unable to create valid connection");
        }

        return connectionWrapper;
    }

    @Override
    public void close() {
    }
}
