package ru.joke.profiler.output.sinks.jdbc;

import ru.joke.profiler.output.sinks.OutputData;
import ru.joke.profiler.output.sinks.ProfilerOutputSinkException;
import ru.joke.profiler.output.sinks.util.injectors.OutputPropertiesInjector;
import ru.joke.profiler.output.sinks.util.pool.ConnectionPool;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

final class OutputDataJdbcStorage implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(OutputDataJdbcStorage.class.getCanonicalName());

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
        this.pool = checkNotNull(pool, "pool");
        checkNotNull(configuration, "configuration");
        this.insertQuery = buildInsertQuery(configuration.outputTableConfiguration());
        this.parametersInjector = checkNotNull(parametersInjector, "parametersInjector");
        this.insertionConfiguration = configuration.dataInsertionConfiguration();

        logger.info("Jdbc storage created with config: " + configuration);
    }

    void init() {
        logger.info("Jdbc storage will be initialized");
        this.pool.init();
        logger.info("Jdbc storage initialized");
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
    public synchronized void close() {
        logger.info("Jdbc storage will be closed");
        this.isClosed = true;
        this.pool.close();
        logger.info("Jdbc storage closed");
    }
}
