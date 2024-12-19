package ru.joke.profiler.core.output.handlers.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.BrokerNotAvailableException;
import org.apache.kafka.common.errors.RetriableException;
import ru.joke.profiler.core.output.handlers.OutputData;
import ru.joke.profiler.core.output.handlers.OutputDataSink;
import ru.joke.profiler.core.output.handlers.ProfilerOutputSinkException;
import ru.joke.profiler.core.output.handlers.util.RecoveryProcessor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

final class KafkaMessageChannel implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(OutputDataSink.class.getCanonicalName());

    private static final String RECOVERY_THREAD_NAME = "profiler-kafka-recovery-thread";

    private final ExecutorService recoveryExecutor;
    private final KafkaProducerSessionFactory producerSessionFactory;
    private final KafkaSinkConfiguration configuration;
    private final KafkaMessageFactory messageFactory;

    private final AtomicBoolean inRecoveryState = new AtomicBoolean(false);

    private volatile KafkaProducerSession producerSession;
    private volatile Future<?> recoveryFuture;
    private volatile boolean isClosed;

    KafkaMessageChannel(
            final KafkaSinkConfiguration configuration,
            final KafkaProducerSessionFactory producerSessionFactory,
            final KafkaMessageFactory messageFactory) {
        this.producerSessionFactory = producerSessionFactory;
        this.configuration = configuration;
        this.messageFactory = messageFactory;
        this.producerSession = producerSessionFactory.create(configuration.producerConfiguration());
        this.recoveryExecutor = Executors.newSingleThreadExecutor(r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(RECOVERY_THREAD_NAME);
            thread.setUncaughtExceptionHandler((t, e) -> logger.log(Level.SEVERE, "Unable to recover connection", e));

            return thread;
        });

    }

    void send(final OutputData data) {
        if (!validateChannelState()) {
            return;
        }

        final ProducerRecord<String, byte[]> message = this.messageFactory.create(data);

        KafkaProducerSession producerSession;
        while ((producerSession = this.producerSession) != null) {

            try {
                producerSession.producer().send(message, (recordMetadata, e) -> {
                    if (e != null) {
                        logger.log(Level.SEVERE, "Unable to send message", e);
                    }
                });

                break;
            } catch (RetriableException | BrokerNotAvailableException ex) {
                if (!tryToRecoverProducerSessionIfNeed(ex, producerSession)) {
                    return;
                }
            } catch (KafkaException ex) {
                if (this.isClosed) {
                    return;
                }

                throw ex;
            }
        }
    }

    @Override
    public synchronized void close() {
        if (this.isClosed) {
            logger.info("Channel already closed");
            return;
        }

        this.isClosed = true;
        if (this.recoveryFuture != null) {
            this.recoveryFuture.cancel(true);
        }

        this.recoveryExecutor.shutdownNow();
        this.producerSession.close();
    }

    private boolean validateChannelState() {
        if (this.inRecoveryState.get()) {
            switch (this.configuration.recoveryConfiguration().processingInRecoveryStatePolicy()) {
            case ERROR:
                throw new ProfilerOutputSinkException("Unable to send output data due to connection recovery");
            case SKIP:
                return false;
            case WAIT:
                return await(this.recoveryFuture);
            }
        }

        return true;
    }

    private boolean await(final Future<?> future) {
        try {
            return future.get() != null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException ex) {
            throw new ProfilerOutputSinkException(ex);
        }
    }

    private synchronized boolean tryToRecoverProducerSessionIfNeed(final Exception ex, final KafkaProducerSession session) {
        final ProcessingInRecoveryStatePolicy policy = this.configuration.recoveryConfiguration().processingInRecoveryStatePolicy();
        if (this.isClosed || this.producerSession != session) {
            return false;
        }

        final boolean goToRecoveryState = this.inRecoveryState.compareAndSet(false, true);
        switch (policy) {
                case WAIT:
                    recoverProducerSession(ex, session);
                    return !this.isClosed;
                case ERROR:
                    if (goToRecoveryState) {
                        this.recoveryFuture = this.recoveryExecutor.submit(() -> recoverProducerSession(ex, session));
                    }

                    throw new ProfilerOutputSinkException("Unable to send output data due to connection recovery", ex);
                case SKIP:
                    if (goToRecoveryState) {
                        this.recoveryFuture = this.recoveryExecutor.submit(() -> recoverProducerSession(ex, session));
                    }

                    return false;
            }

            return false;
    }

    private void recoverProducerSession(final Exception ex, final KafkaProducerSession session) {

        try {
            if (this.isClosed) {
                return;
            }

            // If it doesn't match, it means another thread has already restored the connection => it's safe to attempt message sending
            if (this.producerSession != session) {
                return;
            }

            final RecoveryProcessor recoveryProcessor = new RecoveryProcessor(
                    session::close,
                    () -> this.producerSession = this.producerSessionFactory.create(this.configuration.producerConfiguration()),
                    this.configuration.recoveryConfiguration().maxRetryRecoveryIntervalMs(),
                    this.configuration.recoveryConfiguration().recoveryTimeoutMs()
            );

            try {
                recoveryProcessor.recover(ex);
            } catch (ProfilerOutputSinkException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ProfilerOutputSinkException(e);
            }
        } finally {
            this.inRecoveryState.compareAndSet(true, false);
        }
    }
}