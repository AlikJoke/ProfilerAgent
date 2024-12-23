package ru.joke.profiler.output.handlers.http2;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.entity.AsyncEntityProducers;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;

import java.nio.charset.StandardCharsets;

final class Http2RequestProducerFactory {

    private final String outputEndpoint;
    private final ContentType contentType;
    private final HttpHost targetHost;

    Http2RequestProducerFactory(final Http2SinkConfiguration.OutputMessageConfiguration configuration) {
        this.outputEndpoint = configuration.outputEndpoint();
        this.contentType = ContentType.create(configuration.contentType(), StandardCharsets.UTF_8);
        this.targetHost = new HttpHost(configuration.outputHost(), configuration.outputPort());
    }

    AsyncRequestProducer create(final Http2Message message) {
        final AsyncEntityProducer entityProducer = AsyncEntityProducers.create(
                message.data(),
                this.contentType,
                message.headers()
        );

        return new BasicRequestProducer(
                Method.POST,
                this.targetHost,
                this.outputEndpoint,
                entityProducer
        );
    }
}
