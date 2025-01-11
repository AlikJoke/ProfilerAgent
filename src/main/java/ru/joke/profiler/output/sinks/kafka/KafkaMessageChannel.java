package ru.joke.profiler.output.sinks.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.BrokerNotAvailableException;
import org.apache.kafka.common.errors.RetriableException;
import ru.joke.profiler.output.sinks.OutputData;
import ru.joke.profiler.output.sinks.ProfilerOutputSinkException;
import ru.joke.profiler.output.sinks.util.recovery.ConnectionRecoveryConfiguration;
import ru.joke.profiler.output.sinks.util.recovery.RecoveryProcessor;
import ru.joke.profiler.util.ProfilerThreadFactory;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

final class KafkaMessageChannel implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(KafkaMessageChannel.class.getCanonicalName());

    private static final String RECOVERY_THREAD_NAME = "profiler-kafka-recovery-thread";

    private final ExecutorService recoveryExecutor;
    private final KafkaProducerSessionFactory producerSessionFactory;
    private final KafkaSinkConfiguration configuration;
    private final KafkaMessageFactory messageFactory;
    private final KafkaClusterValidator clusterValidator;

    private final AtomicBoolean inRecoveryState = new AtomicBoolean(false);

    private volatile KafkaProducerSession producerSession;
    private volatile Future<?> recoveryFuture;
    private volatile boolean isClosed;

    KafkaMessageChannel(
            final KafkaSinkConfiguration configuration,
            final KafkaProducerSessionFactory producerSessionFactory,
            final KafkaMessageFactory messageFactory,
            final KafkaClusterValidator clusterValidator
    ) {
        this.producerSessionFactory = checkNotNull(producerSessionFactory, "producerSessionFactory");
        this.clusterValidator = checkNotNull(clusterValidator, "clusterValidator");
        this.configuration = checkNotNull(configuration, "configuration");
        this.messageFactory = checkNotNull(messageFactory, "messageFactory");
        this.producerSession = checkNotNull(producerSessionFactory, "producerSessionFactory").create(configuration.producerConfiguration());
        this.recoveryExecutor = Executors.newSingleThreadExecutor(new ProfilerThreadFactory(RECOVERY_THREAD_NAME, false));
    }
    
    void init() {
        logger.info("Kafka channel will be initialized with config: " + this.configuration);
        final boolean checkClusterOnStart = this.configuration.producerConfiguration().checkClusterOnStart();
        if (checkClusterOnStart) {
            validateClusterAvailable();
        }

        logger.info("Kafka channel initialized");
    }

    void send(final OutputData data) {
        if (!validateChannelState()) {
            return;
        }

        final ProducerRecord<String, byte[]> message = this.messageFactory.create(data);

        KafkaProducerSession producerSession;
        while ((producerSession = this.producerSession) != null) {

            try {
                final KafkaProducerSession session = producerSession;
                session.producer().send(message, (recordMetadata, e) -> {
                    if (e != null) {
                        logger.log(Level.SEVERE, "Unable to send message", e);
                    }

                    if (e instanceof RetriableException || e instanceof BrokerNotAvailableException) {
                        tryToRecoverProducerSessionIfNeed(e, session);
                    }
                });

                break;
            } catch (RetriableException | BrokerNotAvailableException ex) {
                logger.log(Level.WARNING, "Unable to send message to Kafka", ex);
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

        logger.info("Kafka channel will be closed");

        this.isClosed = true;
        if (this.recoveryFuture != null) {
            this.recoveryFuture.cancel(true);
        }

        this.recoveryExecutor.shutdownNow();
        this.producerSession.close();

        logger.info("Kafka channel closed");
    }
    
    private void validateClusterAvailable() {
        final Map<String, String> producerProperties = this.configuration.producerConfiguration().producerProperties();
        if (!this.clusterValidator.isValid(producerProperties)) {
            throw new ProfilerOutputSinkException("Kafka cluster is unavailable");
        }
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
            logger.log(Level.WARNING, "Thread interrupted", ex);
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException ex) {
            throw new ProfilerOutputSinkException(ex);
        }
    }

    private synchronized boolean tryToRecoverProducerSessionIfNeed(final Exception ex, final KafkaProducerSession session) {
        final ConnectionRecoveryConfiguration.ProcessingInRecoveryStatePolicy policy = this.configuration.recoveryConfiguration().processingInRecoveryStatePolicy();
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

    private synchronized void recoverProducerSession(final Exception ex, final KafkaProducerSession session) {

        try {
            // If it doesn't match, it means another thread has already restored the connection => it's safe to attempt message sending
            if (this.isClosed || this.producerSession != session) {
                return;
            }

            logger.fine("Start Kafka connection recovery");

            final RecoveryProcessor recoveryProcessor = new RecoveryProcessor(
                    session::close,
                    () -> {
                        validateClusterAvailable();
                        this.producerSession = this.producerSessionFactory.create(this.configuration.producerConfiguration());
                    },
                    this.configuration.recoveryConfiguration().maxRetryRecoveryIntervalMs(),
                    this.configuration.recoveryConfiguration().recoveryTimeoutMs()
            );

            recoveryProcessor.recover(ex);

            logger.fine("Kafka connection recovered");
        } finally {
            this.inRecoveryState.compareAndSet(true, false);
        }
    }
}
