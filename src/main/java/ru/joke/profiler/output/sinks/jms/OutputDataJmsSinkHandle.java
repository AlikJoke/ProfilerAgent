package ru.joke.profiler.output.sinks.jms;

import ru.joke.profiler.output.sinks.OutputData;
import ru.joke.profiler.output.sinks.OutputDataSink;
import ru.joke.profiler.output.sinks.async.AsyncOutputDataSinkHandleSupport;
import ru.joke.profiler.output.sinks.util.NoProfilingOutputDataSinkWrapper;
import ru.joke.profiler.output.sinks.util.injectors.JsonObjectPropertiesInjector;
import ru.joke.profiler.output.sinks.util.pool.ConnectionFactory;
import ru.joke.profiler.output.sinks.util.pool.ConnectionPoolFactory;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class OutputDataJmsSinkHandle extends AsyncOutputDataSinkHandleSupport<OutputData, JmsSinkConfiguration> {

    public static final String SINK_TYPE = "jms";

    @Override
    public String type() {
        return SINK_TYPE;
    }

    @Override
    protected Class<JmsSinkConfiguration> configurationType() {
        return JmsSinkConfiguration.class;
    }

    @Override
    protected Function<OutputData, Supplier<OutputData>> conversionFunction(
            final JmsSinkConfiguration configuration,
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
            final JmsSinkConfiguration configuration,
            final Map<String, Object> context
    ) {
        final JmsMessageChannel messageChannel = createJmsChannel(configuration);

        final OutputDataSink<OutputData> terminalSink = new OutputDataJmsSink(messageChannel);
        return new NoProfilingOutputDataSinkWrapper<>(terminalSink);
    }

    @Override
    protected OutputDataSink<OutputData> createSyncOutputSink(
            final JmsSinkConfiguration configuration,
            final Map<String, Object> context
    ) {
        return createTerminalOutputSink(configuration, context);
    }

    private JmsMessageChannel createJmsChannel(final JmsSinkConfiguration configuration) {
        final JmsSinkConfiguration.OutputMessageConfiguration messageConfiguration = configuration.outputMessageConfiguration();
        final JsonObjectPropertiesInjector bodyInjector = new JsonObjectPropertiesInjector(messageConfiguration.bodyPropertiesMapping());
        final JmsProducerPropertiesInjector producerPropertiesInjector = new JmsProducerPropertiesInjector(messageConfiguration.messagePropertiesMapping());

        final ConnectionFactory<JmsContextWrapper> connectionFactory = new JmsConnectionFactory(configuration.outputDestinationConfiguration());
        final ConnectionPoolFactory<JmsContextWrapper> connectionPoolFactory = new ConnectionPoolFactory<>(connectionFactory);

        return new JmsMessageChannel(
                configuration,
                connectionPoolFactory,
                bodyInjector,
                producerPropertiesInjector
        );
    }

}
