package ru.joke.profiler.output.sinks.http2;

import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.entity.DiscardingEntityConsumer;
import org.apache.hc.core5.http.nio.support.BasicResponseConsumer;
import ru.joke.profiler.output.sinks.OutputData;
import ru.joke.profiler.output.sinks.ProfilerOutputSinkException;

import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

final class Http2MessageChannel implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(Http2MessageChannel.class.getCanonicalName());

    private final Http2SinkConfiguration.ProcessingConfiguration configuration;
    private final Http2Client httpClient;
    private final Http2MessageFactory messageEntityFactory;
    private final Http2RequestProducerFactory requestProducerFactory;

    private final Map<Http2Message, Future<?>> activeRequests;

    Http2MessageChannel(
            final Http2SinkConfiguration.ProcessingConfiguration configuration,
            final Http2ClientFactory http2ClientFactory,
            final Http2MessageFactory messageEntityFactory,
            final Http2RequestProducerFactory requestProducerFactory
    ) {
        this.configuration = configuration;
        this.httpClient = http2ClientFactory.create();
        this.messageEntityFactory = messageEntityFactory;
        this.requestProducerFactory = requestProducerFactory;
        this.activeRequests = new ConcurrentHashMap<>();
    }

    void send(final OutputData outputData) {
        final Http2Message message = this.messageEntityFactory.create(outputData);
        sendRequest(message, 0);
    }

    void init() {
        logger.info("Http2 message channel initializing with config: " + this.configuration);
        this.httpClient.init();
        logger.info("Http2 message channel initialized");
    }

    @Override
    public synchronized void close() {
        logger.info("Http2 message channel will be closed");
        this.httpClient.close();
        logger.info("Http2 message channel closed");
    }

    private void sendRequest(final Http2Message message, final int requestId) {

        final AsyncRequestProducer requestProducer = this.requestProducerFactory.create(message);
        final Future<?> requestFuture = this.httpClient.execute(
                requestProducer,
                new BasicResponseConsumer<>(new DiscardingEntityConsumer<>()),
                new FutureCallback<Message<HttpResponse, String>>() {
                    @Override
                    public void completed(final Message<HttpResponse, String> response) {
                        final int responseCode = response.getHead().getCode();

                        final boolean asyncEnabled = activeRequests.remove(message) != null;
                        if (responseCode == HttpStatus.SC_OK || responseCode == HttpStatus.SC_NO_CONTENT || responseCode == HttpStatus.SC_CREATED) {
                            return;
                        }

                        if (asyncEnabled) {
                            handleOnError(message, requestId, null);
                        }
                    }

                    @Override
                    public void failed(final Exception e) {
                        logger.log(Level.WARNING, "Error on send profiling data", e);
                        handleOnError(message, requestId, e);
                    }

                    @Override
                    public void cancelled() {
                        logger.log(Level.FINEST, "Http2 request was cancelled");
                        activeRequests.remove(message);
                    }
                });

        if (this.configuration.disableAsyncSending()) {
            awaitResponse(
                    message,
                    requestFuture,
                    this.configuration.syncSendingWaitMs(),
                    requestId
            );
        } else {
            this.activeRequests.put(message, requestFuture);
        }
    }

    private void awaitResponse(
            final Http2Message message,
            final Future<?> future,
            final long timeout,
            final int requestId
    ) {
        try {
            future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | ExecutionException e) {
            if (!handleOnError(message, requestId, e)) {
                throw new ProfilerOutputSinkException(e);
            }
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Thread interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    private boolean handleOnError(
            final Http2Message message,
            final int requestId,
            final Exception ex
    ) {
        if (this.configuration.onErrorPolicy() != Http2SinkConfiguration.ProcessingConfiguration.OnErrorPolicy.RETRY) {
            return true;
        }

        if (this.httpClient.isClosed()) {
            logger.info("Message will be skipped due to shutdown");
            return true;
        }

        if (requestId < this.configuration.maxRetriesOnError()) {
            sendRequest(message, requestId + 1);
            return true;
        }

        if (ex == null) {
            logger.severe("Error on message sending (server return disallowed response code)");
        } else {
            logger.log(Level.SEVERE, "Error on message sending", ex);
        }

        return false;
    }
}
