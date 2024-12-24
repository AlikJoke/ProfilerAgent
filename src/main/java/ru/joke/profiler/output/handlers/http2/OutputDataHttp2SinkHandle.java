package ru.joke.profiler.output.handlers.http2;

import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.OutputDataSink;
import ru.joke.profiler.output.handlers.async.AsyncOutputDataSinkHandleSupport;
import ru.joke.profiler.output.handlers.util.JsonObjectPropertiesInjector;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class OutputDataHttp2SinkHandle extends AsyncOutputDataSinkHandleSupport<OutputData> {

    public static final String SINK_TYPE = "http2";

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

        final Http2SinkConfigurationLoader configurationLoader = new Http2SinkConfigurationLoader();
        final Http2SinkConfiguration configuration = configurationLoader.load(properties);

        final Http2MessageChannel channel = buildMessageChannel(configuration);
        return new OutputDataHttp2Sink(channel);
    }

    @Override
    protected OutputDataSink<OutputData> createSyncOutputSink(
            final Map<String, String> properties,
            final Map<String, Object> context) {
        return createTerminalOutputSink(properties, context);
    }

    private Http2MessageChannel buildMessageChannel(final Http2SinkConfiguration configuration) {

        final Http2SinkConfiguration.OutputMessageConfiguration outputMessageConfiguration = configuration.outputMessageConfiguration();
        final Http2RequestProducerFactory requestProducerFactory = new Http2RequestProducerFactory(outputMessageConfiguration);
        final Http2MessageFactory messageFactory = buildMessageFactory(outputMessageConfiguration);
        final Http2ClientFactory clientFactory = new Http2ClientFactory(configuration.http2ClientConfiguration());

        return new Http2MessageChannel(
                configuration.processingConfiguration(),
                clientFactory,
                messageFactory,
                requestProducerFactory
        );
    }

    private Http2MessageFactory buildMessageFactory(final Http2SinkConfiguration.OutputMessageConfiguration configuration) {
        final JsonObjectPropertiesInjector bodyInjector = new JsonObjectPropertiesInjector(configuration.propertiesMapping());
        final Http2HeaderPropertiesInjector headerPropertiesInjector = new Http2HeaderPropertiesInjector(configuration.headersMapping());

        return new Http2MessageFactory(
                configuration,
                bodyInjector,
                headerPropertiesInjector
        );
    }
}
