package ru.joke.profiler.output.sinks.http2;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorStatus;
import ru.joke.profiler.output.sinks.ProfilerOutputSinkException;

import java.util.concurrent.Future;
import java.util.logging.Logger;

final class Http2Client implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(Http2Client.class.getCanonicalName());

    private final CloseableHttpAsyncClient delegateClient;
    private final Http2SinkConfiguration.Http2ClientConfiguration http2ClientConfiguration;

    Http2Client(
            final CloseableHttpAsyncClient delegateClient,
            final Http2SinkConfiguration.Http2ClientConfiguration http2ClientConfiguration
    ) {
        this.delegateClient = delegateClient;
        this.http2ClientConfiguration = http2ClientConfiguration;
    }

    void init() {
        logger.info("Http2Client will be started");
        this.delegateClient.start();
        logger.info("Http2Client started with configuration: " + this.http2ClientConfiguration);
    }

    <T> Future<T> execute(
            final AsyncRequestProducer requestProducer,
            final AsyncResponseConsumer<T> responseConsumer,
            final FutureCallback<T> callback
    ) {
        return this.delegateClient.execute(requestProducer, responseConsumer, callback);
    }

    synchronized boolean isClosed() {
        final IOReactorStatus status = this.delegateClient.getStatus();
        return status == IOReactorStatus.SHUT_DOWN || status == IOReactorStatus.SHUTTING_DOWN;
    }

    @Override
    public synchronized void close() {
        if (isClosed()) {
            throw new ProfilerOutputSinkException("Http2 client already closed");
        }

        final CloseMode closeMode = http2ClientConfiguration.enableGracefulShutdown() ? CloseMode.GRACEFUL : CloseMode.IMMEDIATE;
        logger.info("Http2Client will be closed with mode " + closeMode);

        this.delegateClient.close(closeMode);

        logger.info("Http2Client closed");
    }
}
