package ru.joke.profiler.output.sinks.kafka;

import ru.joke.profiler.output.sinks.OutputData;
import ru.joke.profiler.output.sinks.OutputDataSink;
import ru.joke.profiler.output.sinks.async.AsyncOutputDataSinkHandleSupport;
import ru.joke.profiler.output.sinks.util.NoProfilingOutputDataSinkWrapper;
import ru.joke.profiler.output.sinks.util.injectors.JsonObjectPropertiesInjector;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class OutputDataKafkaSinkHandle extends AsyncOutputDataSinkHandleSupport<OutputData, KafkaSinkConfiguration> {

    public static final String SINK_TYPE = "kafka";

    @Override
    public String type() {
        return SINK_TYPE;
    }

    @Override
    protected Class<KafkaSinkConfiguration> configurationType() {
        return KafkaSinkConfiguration.class;
    }

    @Override
    protected Function<OutputData, Supplier<OutputData>> conversionFunction(
            final KafkaSinkConfiguration configuration,
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
            final KafkaSinkConfiguration configuration,
            final Map<String, Object> context
    ) {
        final KafkaMessageChannel kafkaMessageChannel = createKafkaChannel(configuration);

        final OutputDataSink<OutputData> terminalSink = new OutputDataKafkaSink(kafkaMessageChannel);
        return new NoProfilingOutputDataSinkWrapper<>(terminalSink);
    }

    @Override
    protected OutputDataSink<OutputData> createSyncOutputSink(
            final KafkaSinkConfiguration configuration,
            final Map<String, Object> context
    ) {
        return createTerminalOutputSink(configuration, context);
    }

    private KafkaMessageChannel createKafkaChannel(final KafkaSinkConfiguration configuration) {
        final KafkaSinkConfiguration.OutputRecordConfiguration recordConfiguration = configuration.outputRecordConfiguration();
        final JsonObjectPropertiesInjector injector = new JsonObjectPropertiesInjector(recordConfiguration.propertiesMapping());

        final KafkaProducerSessionFactory producerSessionFactory = new KafkaProducerSessionFactory();
        final Function<OutputData, byte[]> bodyConversionFunc = o -> injector.inject(new StringBuilder(), o).toString().getBytes(StandardCharsets.UTF_8);
        final KafkaHeaderPropertiesInjector headerPropertiesInjector = new KafkaHeaderPropertiesInjector(recordConfiguration.headersMapping());
        final KafkaMessageFactory messageFactory = new KafkaMessageFactory(
                recordConfiguration,
                headerPropertiesInjector,
                bodyConversionFunc
        );

        final KafkaClusterValidator clusterValidator = new KafkaClusterValidator();

        return new KafkaMessageChannel(
                configuration,
                producerSessionFactory,
                messageFactory,
                clusterValidator
        );
    }

}
