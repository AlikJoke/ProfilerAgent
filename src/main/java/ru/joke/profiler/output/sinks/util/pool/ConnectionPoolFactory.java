package ru.joke.profiler.output.sinks.util.pool;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

public final class ConnectionPoolFactory<T extends PooledConnection> {

    private final ConnectionFactory<T> connectionFactory;

    public ConnectionPoolFactory(final ConnectionFactory<T> connectionFactory) {
        this.connectionFactory = checkNotNull(connectionFactory, "connectionFactory");
    }

    public ConnectionPool<T> create(final ConnectionPoolConfiguration configuration) {
        return configuration.enablePooling()
                ? new StdConnectionPool<>(this.connectionFactory, configuration)
                : new NoPoolingConnectionPool<>(this.connectionFactory);
    }
}
