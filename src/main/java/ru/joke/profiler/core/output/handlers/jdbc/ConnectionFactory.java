package ru.joke.profiler.core.output.handlers.jdbc;

import ru.joke.profiler.core.output.handlers.ProfilerOutputSinkException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

final class ConnectionFactory {

    private final JdbcSinkConfiguration.ConnectionFactoryConfiguration configuration;

    ConnectionFactory(final JdbcSinkConfiguration.ConnectionFactoryConfiguration configuration) {
        this.configuration = configuration;
    }

    Connection create() {
        try {
            return DriverManager.getConnection(this.configuration.url(), this.configuration.connectionProperties());
        } catch (SQLException e) {
            throw new ProfilerOutputSinkException(e);
        }
    }
}
