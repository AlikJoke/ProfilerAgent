package ru.joke.profiler.output.sinks.jdbc;

import ru.joke.profiler.output.sinks.ProfilerOutputSinkException;
import ru.joke.profiler.output.sinks.util.pool.ConnectionFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

final class JdbcConnectionFactory implements ConnectionFactory<JdbcConnectionWrapper> {

    private final JdbcSinkConfiguration.ConnectionFactoryConfiguration configuration;

    JdbcConnectionFactory(final JdbcSinkConfiguration.ConnectionFactoryConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public JdbcConnectionWrapper create() {
        return new JdbcConnectionWrapper(this::createJdbcConnection);
    }

    private Connection createJdbcConnection() {
        try {
            return DriverManager.getConnection(this.configuration.url(), this.configuration.connectionProperties());
        } catch (SQLException e) {
            throw new ProfilerOutputSinkException(e);
        }
    }
}
