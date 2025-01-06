package ru.joke.profiler.output.sinks.http2;

import ru.joke.profiler.output.sinks.OutputData;
import ru.joke.profiler.output.sinks.OutputDataSink;
import ru.joke.profiler.output.sinks.async.AsyncOutputDataSinkHandleSupport;
import ru.joke.profiler.output.sinks.util.injectors.JsonObjectPropertiesInjector;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class OutputDataHttp2SinkHandle extends AsyncOutputDataSinkHandleSupport<OutputData, Http2SinkConfiguration> {

    public static final String SINK_TYPE = "http2";

    @Override
    public String type() {
        return SINK_TYPE;
    }

    @Override
    protected Class<Http2SinkConfiguration> configurationType() {
        return Http2SinkConfiguration.class;
    }

    @Override
    protected Function<OutputData, Supplier<OutputData>> conversionFunction(
            final Http2SinkConfiguration configuration,
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
            final Http2SinkConfiguration configuration,
            final Map<String, Object> context
    ) {
        final Http2MessageChannel channel = buildMessageChannel(configuration);
        return new OutputDataHttp2Sink(channel);
    }

    @Override
    protected OutputDataSink<OutputData> createSyncOutputSink(
            final Http2SinkConfiguration configuration,
            final Map<String, Object> context
    ) {
        return createTerminalOutputSink(configuration, context);
    }

    private Http2MessageChannel buildMessageChannel(final Http2SinkConfiguration configuration) {

        final Http2SinkConfiguration.OutputMessageConfiguration outputMessageConfiguration = configuration.outputMessageConfiguration();
        final Http2SinkConfiguration.OutputEndpointConfiguration outputEndpointConfiguration = configuration.outputEndpointConfiguration();
        final Http2RequestProducerFactory requestProducerFactory = new Http2RequestProducerFactory(outputEndpointConfiguration, outputMessageConfiguration);

        final Http2MessageFactory messageFactory = buildMessageFactory(outputMessageConfiguration);
        final Http2ClientCredentialsProviderFactory credentialsProviderFactory = new Http2ClientCredentialsProviderFactory(outputEndpointConfiguration);
        final Http2ClientFactory clientFactory = new Http2ClientFactory(configuration.http2ClientConfiguration(), credentialsProviderFactory);

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
