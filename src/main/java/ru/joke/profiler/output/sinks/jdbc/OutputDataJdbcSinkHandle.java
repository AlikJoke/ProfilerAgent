package ru.joke.profiler.output.sinks.jdbc;

import ru.joke.profiler.output.sinks.OutputData;
import ru.joke.profiler.output.sinks.OutputDataSink;
import ru.joke.profiler.output.sinks.async.AsyncOutputDataSinkHandleSupport;
import ru.joke.profiler.output.sinks.util.NoProfilingOutputDataSinkWrapper;
import ru.joke.profiler.output.sinks.util.injectors.OutputPropertiesInjector;
import ru.joke.profiler.output.sinks.util.pool.ConnectionFactory;
import ru.joke.profiler.output.sinks.util.pool.ConnectionPool;
import ru.joke.profiler.output.sinks.util.pool.ConnectionPoolFactory;

import java.sql.PreparedStatement;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class OutputDataJdbcSinkHandle extends AsyncOutputDataSinkHandleSupport<OutputData, JdbcSinkConfiguration> {

    public static final String SINK_TYPE = "jdbc";

    @Override
    public String type() {
        return SINK_TYPE;
    }

    @Override
    protected Class<JdbcSinkConfiguration> configurationType() {
        return JdbcSinkConfiguration.class;
    }

    @Override
    protected Function<OutputData, Supplier<OutputData>> conversionFunction(
            final JdbcSinkConfiguration configuration,
            final Map<String, Object> context
    ) {
        return o -> {
            final OutputData data = new OutputData();
            data.fill(o);
            return () -> data;
        };
    }

    @Override
    protected OutputDataSink<OutputData> createTerminalOutputSink(
            final JdbcSinkConfiguration configuration,
            final Map<String, Object> context
    ) {
        final ConnectionFactory<JdbcConnectionWrapper> connectionFactory = new JdbcConnectionFactory(configuration.connectionFactoryConfiguration());
        final ConnectionPoolFactory<JdbcConnectionWrapper> poolFactory = new ConnectionPoolFactory<>(connectionFactory);

        final ConnectionPool<JdbcConnectionWrapper> pool = poolFactory.create(configuration.connectionPoolConfiguration());

        final OutputDataSink<OutputData> terminalSink = buildJdbcSink(configuration, pool, connectionFactory);
        return new NoProfilingOutputDataSinkWrapper<>(terminalSink);
    }

    @Override
    protected OutputDataSink<OutputData> createSyncOutputSink(
            final JdbcSinkConfiguration configuration,
            final Map<String, Object> context
    ) {
        return createTerminalOutputSink(configuration, context);
    }

    private OutputDataSink<OutputData> buildJdbcSink(
            final JdbcSinkConfiguration configuration,
            final ConnectionPool<JdbcConnectionWrapper> pool,
            final ConnectionFactory<JdbcConnectionWrapper> connectionFactory
    ) {
        final OutputPropertiesInjector<PreparedStatement> propertiesInjector = new JdbcStatementPropertiesInjector(configuration.outputTableConfiguration());
        final OutputDataJdbcStorage jdbcStorage = new OutputDataJdbcStorage(pool, configuration, propertiesInjector);

        final OutputDataTableSchemaValidator tableSchemaValidator = new OutputDataTableSchemaValidator(configuration.outputTableConfiguration());
        final OutputDataTablePreparer outputDataTablePreparer =
                new OutputDataTablePreparer(
                        configuration.outputTableConfiguration(), connectionFactory,
                        tableSchemaValidator
                );

        return new OutputDataJdbcSink(jdbcStorage, outputDataTablePreparer);
    }
}
