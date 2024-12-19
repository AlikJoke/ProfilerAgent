package ru.joke.profiler.core.output.handlers.jdbc;

final class ConnectionPoolFactory {

    private final ConnectionFactory connectionFactory;

    ConnectionPoolFactory(final ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    ConnectionPool create(final JdbcSinkConfiguration.ConnectionPoolConfiguration configuration) {
        return configuration.enablePooling()
                ? new StdConnectionPool(this.connectionFactory, configuration)
                : new NoPoolingConnectionPool(this.connectionFactory);
    }
}
