package ru.joke.profiler.core.output.handlers.jdbc;

import ru.joke.profiler.core.output.handlers.ProfilerOutputSinkException;

import java.sql.Connection;
import java.sql.SQLException;

final class NoPoolingConnectionPool implements ConnectionPool {

    private final ConnectionFactory connectionFactory;

    public NoPoolingConnectionPool(final ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void init() {
    }

    @Override
    public void release(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new ProfilerOutputSinkException(e);
        }
    }

    @Override
    public Connection get() {
        return this.connectionFactory.create();
    }

    @Override
    public void close() {
    }
}
