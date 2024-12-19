package ru.joke.profiler.core.output.handlers.kafka;

import ru.joke.profiler.core.output.handlers.OutputData;
import ru.joke.profiler.core.output.handlers.OutputDataConversionSinkWrapper;
import ru.joke.profiler.core.output.handlers.OutputDataSink;
import ru.joke.profiler.core.output.handlers.async.AsyncOutputDataSinkHandleSupport;
import ru.joke.profiler.core.output.handlers.util.JsonObjectPropertiesInjector;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class OutputDataKafkaSinkHandle extends AsyncOutputDataSinkHandleSupport<OutputData> {

    public static final String SINK_TYPE = "kafka";

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
        final KafkaSinkConfigurationLoader configurationLoader = new KafkaSinkConfigurationLoader();
        final KafkaSinkConfiguration configuration = configurationLoader.load(properties);

        final KafkaMessageChannel kafkaMessageChannel = createKafkaChannel(configuration);

        return new OutputDataKafkaSink(kafkaMessageChannel);
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

    private KafkaMessageChannel createKafkaChannel(final KafkaSinkConfiguration configuration) {
        final KafkaSinkConfiguration.OutputMessageConfiguration messageConfiguration = configuration.outputMessageConfiguration();
        final JsonObjectPropertiesInjector injector = new JsonObjectPropertiesInjector(messageConfiguration.propertiesMapping());

        final KafkaProducerSessionFactory producerSessionFactory = new KafkaProducerSessionFactory();
        final Function<OutputData, byte[]> bodyConversionFunc = o -> injector.inject(new StringBuilder(), o).toString().getBytes(StandardCharsets.UTF_8);
        final KafkaHeaderPropertiesInjector headerPropertiesInjector = new KafkaHeaderPropertiesInjector(messageConfiguration.headersMapping());
        final KafkaMessageFactory messageFactory = new KafkaMessageFactory(
                messageConfiguration,
                headerPropertiesInjector,
                bodyConversionFunc
        );

        return new KafkaMessageChannel(
                configuration,
                producerSessionFactory,
                messageFactory
        );
    }

}
