package ru.joke.profiler.core.output.handlers.jdbc;

import ru.joke.profiler.core.output.handlers.OutputData;
import ru.joke.profiler.core.output.handlers.util.NoProfilingOutputDataSinkWrapper;
import ru.joke.profiler.core.output.handlers.util.OutputDataConversionSinkWrapper;
import ru.joke.profiler.core.output.handlers.OutputDataSink;
import ru.joke.profiler.core.output.handlers.util.OutputPropertiesInjector;
import ru.joke.profiler.core.output.handlers.async.AsyncOutputDataSinkHandleSupport;

import java.sql.PreparedStatement;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class OutputDataJdbcSinkHandle extends AsyncOutputDataSinkHandleSupport<OutputData> {

    public static final String SINK_TYPE = "jdbc";

    @Override
    public String type() {
        return SINK_TYPE;
    }

    @Override
    protected Function<OutputData, Supplier<OutputData>> conversionFunction(
            final Map<String, String> properties,
            final Map<String, Object> context) {
        return o -> {
            final OutputData data = new OutputData();
            data.fill(o);
            return () -> data;
        };
    }

    @Override
    protected OutputDataSink<OutputData> createTerminalOutputSink(
            final Map<String, String> properties,
            final Map<String, Object> context) {
        final JdbcSinkConfigurationLoader configurationLoader = new JdbcSinkConfigurationLoader();
        final JdbcSinkConfiguration configuration = configurationLoader.load(properties);

        final ConnectionFactory connectionFactory = new ConnectionFactory(configuration.connectionFactoryConfiguration());
        final ConnectionPoolFactory poolFactory = new ConnectionPoolFactory(connectionFactory);

        final ConnectionPool pool = poolFactory.create(configuration.connectionPoolConfiguration());

        final OutputPropertiesInjector<PreparedStatement> propertiesInjector = new JdbcStatementPropertiesInjector(configuration.outputTableConfiguration());
        final OutputDataJdbcStorage jdbcStorage = new OutputDataJdbcStorage(pool, configuration, propertiesInjector);

        final OutputDataTableSchemaValidator tableSchemaValidator = new OutputDataTableSchemaValidator(configuration.outputTableConfiguration());
        final OutputDataTablePreparer outputDataTablePreparer =
                new OutputDataTablePreparer(
                        configuration.outputTableConfiguration(),
                        connectionFactory,
                        tableSchemaValidator
                );

        final OutputDataSink<OutputData> terminalSink = new OutputDataJdbcSink(jdbcStorage, outputDataTablePreparer);
        return new NoProfilingOutputDataSinkWrapper<>(terminalSink);
    }

    @Override
    protected OutputDataSink<OutputData> createSyncOutputSink(
            final Map<String, String> properties,
            final Map<String, Object> context) {
        return new OutputDataConversionSinkWrapper<>(
                createTerminalOutputSink(properties, context),
                Function.identity()
        );
    }
}
