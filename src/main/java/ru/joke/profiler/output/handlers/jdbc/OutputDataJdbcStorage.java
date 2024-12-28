package ru.joke.profiler.output.handlers.jdbc;

import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.ProfilerOutputSinkException;
import ru.joke.profiler.output.handlers.util.injectors.OutputPropertiesInjector;
import ru.joke.profiler.output.handlers.util.pool.ConnectionPool;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

final class OutputDataJdbcStorage implements AutoCloseable {

    private static final String INSERT_QUERY_TEMPLATE = "INSERT INTO %s(%s) VALUES(%s)";

    private final ConnectionPool<JdbcConnectionWrapper> pool;
    private final String insertQuery;
    private final OutputPropertiesInjector<PreparedStatement> parametersInjector;
    private final JdbcSinkConfiguration.OutputDataInsertionConfiguration insertionConfiguration;

    private volatile boolean isClosed;

    OutputDataJdbcStorage(
            final ConnectionPool<JdbcConnectionWrapper> pool,
            final JdbcSinkConfiguration configuration,
            final OutputPropertiesInjector<PreparedStatement> parametersInjector
    ) {
        this.pool = pool;
        this.insertQuery = buildInsertQuery(configuration.outputTableConfiguration());
        this.parametersInjector = parametersInjector;
        this.insertionConfiguration = configuration.dataInsertionConfiguration();
    }

    void init() {
        this.pool.init();
    }

    void store(final OutputData data) {
        final JdbcConnectionWrapper connection = tryToTakeConnection();
        if (connection == null) {
            return;
        }

        try (final PreparedStatement statement = connection.prepareStatement(this.insertQuery)) {
            connection.setAutoCommit(true);

            putParameters(statement, data);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (this.isClosed) {
                return;
            }

            throw new ProfilerOutputSinkException(e);
        } finally {
            this.pool.release(connection);
        }
    }

    void store(final List<OutputData> data) {
        final JdbcConnectionWrapper connection = tryToTakeConnection();
        if (connection == null) {
            return;
        }

        try (final PreparedStatement statement = connection.prepareStatement(this.insertQuery)) {
            connection.setAutoCommit(true);

            int currentBatchSize = 0;
            for (final OutputData params : data) {
                putParameters(statement, params);

                if (this.insertionConfiguration.enableBatching()) {
                    statement.addBatch();

                    if (++currentBatchSize == this.insertionConfiguration.batchSize()) {
                        statement.executeBatch();
                        currentBatchSize = 0;
                    }
                } else {
                    statement.executeUpdate();
                }

                statement.clearParameters();
            }

            if (this.insertionConfiguration.enableBatching() && currentBatchSize > 0) {
                statement.executeBatch();
            }
        } catch (SQLException e) {
            if (this.isClosed) {
                return;
            }

            throw new ProfilerOutputSinkException(e);
        } finally {
            this.pool.release(connection);
        }
    }

    private JdbcConnectionWrapper tryToTakeConnection() {
        try {
            return this.pool.get();
        } catch (ProfilerOutputSinkException ex) {
            if (this.isClosed) {
                return null;
            }

            throw ex;
        }
    }

    private void putParameters(final PreparedStatement statement, final OutputData parameters) {
        this.parametersInjector.inject(statement, parameters);
    }

    private String buildInsertQuery(final JdbcSinkConfiguration.OutputTableConfiguration configuration) {
        final String columns =
                configuration.columnsMetadata()
                                .values()
                                .stream()
                                .map(JdbcSinkConfiguration.OutputTableConfiguration.ColumnMetadata::columnName)
                                .collect(Collectors.joining(","));
        final Collection<String> preparedParameters = Collections.nCopies(configuration.columnsMetadata().size(), "?");
        final String preparedParametersStr = String.join(",", preparedParameters);
        return String.format(INSERT_QUERY_TEMPLATE, configuration.tableName(), columns, preparedParametersStr);
    }

    @Override
    public void close() {
        this.isClosed = true;
        this.pool.close();
    }
}
