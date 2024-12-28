package ru.joke.profiler.output.handlers.jms;

import ru.joke.profiler.configuration.meta.ConfigurationParser;
import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.OutputDataSink;
import ru.joke.profiler.output.handlers.async.AsyncOutputDataSinkHandleSupport;
import ru.joke.profiler.output.handlers.util.injectors.JsonObjectPropertiesInjector;
import ru.joke.profiler.output.handlers.util.NoProfilingOutputDataSinkWrapper;
import ru.joke.profiler.output.handlers.util.pool.ConnectionFactory;
import ru.joke.profiler.output.handlers.util.pool.ConnectionPoolFactory;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class OutputDataJmsSinkHandle extends AsyncOutputDataSinkHandleSupport<OutputData> {

    public static final String SINK_TYPE = "jms";

    @Override
    public String type() {
        return SINK_TYPE;
    }

    @Override
    protected Function<OutputData, Supplier<OutputData>> conversionFunction(
            final Map<String, String> properties,
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
            final Map<String, String> properties,
            final Map<String, Object> context
    ) {
        final JmsSinkConfiguration configuration = ConfigurationParser.parse(JmsSinkConfiguration.class, properties);

        final JmsMessageChannel messageChannel = createJmsChannel(configuration);

        final OutputDataSink<OutputData> terminalSink = new OutputDataJmsSink(messageChannel);
        return new NoProfilingOutputDataSinkWrapper<>(terminalSink);
    }

    @Override
    protected OutputDataSink<OutputData> createSyncOutputSink(
            final Map<String, String> properties,
            final Map<String, Object> context
    ) {
        return createTerminalOutputSink(properties, context);
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
