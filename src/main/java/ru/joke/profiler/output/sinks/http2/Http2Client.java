package ru.joke.profiler.output.sinks.http2;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorStatus;
import ru.joke.profiler.output.sinks.ProfilerOutputSinkException;

import java.util.concurrent.Future;

final class Http2Client implements AutoCloseable {

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
        this.delegateClient.start();
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
        this.delegateClient.close(closeMode);
    }
}
