package ru.joke.profiler.output.handlers.jms;

import jakarta.jms.DeliveryMode;
import jakarta.jms.Destination;
import jakarta.jms.JMSProducer;
import jakarta.jms.JMSRuntimeException;
import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.OutputDataSink;
import ru.joke.profiler.output.handlers.ProfilerOutputSinkException;
import ru.joke.profiler.output.handlers.util.injectors.OutputPropertiesInjector;
import ru.joke.profiler.output.handlers.util.pool.ConnectionPool;
import ru.joke.profiler.output.handlers.util.pool.ConnectionPoolFactory;
import ru.joke.profiler.output.handlers.util.recovery.ConnectionRecoveryConfiguration;
import ru.joke.profiler.output.handlers.util.recovery.RecoveryProcessor;
import ru.joke.profiler.util.ProfilerThreadFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

final class JmsMessageChannel implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(OutputDataSink.class.getCanonicalName());

    private static final String RECOVERY_THREAD_NAME = "profiler-jms-recovery-thread";

    private final ExecutorService recoveryExecutor;
    private final JmsSinkConfiguration configuration;
    private final ConnectionPool<JmsContextWrapper> connectionPool;
    private final OutputPropertiesInjector<StringBuilder> outputMessageBodyBuilder;
    private final OutputPropertiesInjector<JMSProducer> producerPropertiesInjector;
    private final Destination outputEndpoint;

    private final AtomicBoolean inRecoveryState = new AtomicBoolean(false);

    private volatile Future<?> recoveryFuture;
    private volatile boolean isClosed;

    JmsMessageChannel(
            final JmsSinkConfiguration configuration,
            final ConnectionPoolFactory<JmsContextWrapper> connectionPoolFactory,
            final OutputPropertiesInjector<StringBuilder> outputMessageBodyBuilder,
            final OutputPropertiesInjector<JMSProducer> producerPropertiesInjector
    ) {
        this.connectionPool = connectionPoolFactory.create(configuration.connectionPoolConfiguration());
        this.configuration = configuration;
        this.outputMessageBodyBuilder = outputMessageBodyBuilder;
        this.producerPropertiesInjector = producerPropertiesInjector;
        this.outputEndpoint = lookup(configuration.outputDestinationConfiguration().destinationJndiName());
        this.recoveryExecutor = Executors.newSingleThreadExecutor(new ProfilerThreadFactory(RECOVERY_THREAD_NAME, false));
    }
    
    synchronized void init() {
        this.connectionPool.init();
    }

    void send(final OutputData data) {
        send(Collections.singletonList(data));
    }

    void send(final List<OutputData> outputData) {
        if (!validateChannelState()) {
            return;
        }

        final byte[][] outputBodies = composeOutputMessagesBody(outputData);

        boolean retry;
        do {
            retry = false;
            JmsContextWrapper session = null;
            try {
                session = this.connectionPool.get();
                if (session == null) {
                    return;
                }

                sendMessage(outputBodies, outputData, session);
            } catch (ProfilerOutputSinkException | JMSRuntimeException ex) {
                retry = tryToRecoverProducerSessionIfNeed(ex, session, this.inRecoveryState.compareAndSet(false, true));
            } finally {
                if (session != null) {
                    this.connectionPool.release(session);
                }
            }
        } while (retry);
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
        this.connectionPool.close();
    }

    private byte[][] composeOutputMessagesBody(final List<OutputData> data) {
        final byte[][] result = new byte[data.size()][];

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < data.size(); i++) {
            builder = this.outputMessageBodyBuilder.inject(builder, data.get(i));
            result[i] = builder.toString().getBytes(StandardCharsets.UTF_8);
        }

        return result;
    }

    private void sendMessage(
            final byte[][] outputBodies,
            final List<OutputData> outputData,
            final JmsContextWrapper session
    ) {
        final JmsSinkConfiguration.OutputMessageConfiguration outputMessageConfiguration = this.configuration.outputMessageConfiguration();
        final JMSProducer producer =
                session.createProducer()
                        .setJMSType(outputMessageConfiguration.messageType())
                        .setDisableMessageID(!outputMessageConfiguration.includeMessageId())
                        .setTimeToLive(outputMessageConfiguration.ttlMs())
                        .setDeliveryDelay(outputMessageConfiguration.deliveryDelayMs())
                        .setDisableMessageTimestamp(!outputMessageConfiguration.includeMessageTimestamp())
                        .setDeliveryMode(outputMessageConfiguration.persistent() ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);

        for (int i = 0; i < outputData.size(); i++) {
            this.producerPropertiesInjector.inject(producer, outputData.get(i));
            producer.send(this.outputEndpoint, outputBodies[i]);
            producer.clearProperties();
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
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException ex) {
            throw new ProfilerOutputSinkException(ex);
        }
    }

    private synchronized boolean tryToRecoverProducerSessionIfNeed(
            final Exception ex,
            final JmsContextWrapper session,
            final boolean switchedToRecoveryState
    ) {
        if (this.isClosed) {
            return false;
        }

        final ConnectionRecoveryConfiguration recoveryConfiguration = this.configuration.recoveryConfiguration();
        final ConnectionRecoveryConfiguration.ProcessingInRecoveryStatePolicy policy = recoveryConfiguration.processingInRecoveryStatePolicy();
        switch (policy) {
            case WAIT:
                if (switchedToRecoveryState) {
                    recoverProducerSession(ex, session);
                }
                return !this.isClosed;
            case ERROR:
                if (switchedToRecoveryState) {
                    this.recoveryFuture = this.recoveryExecutor.submit(() -> recoverProducerSession(ex, session));
                }

                throw new ProfilerOutputSinkException("Unable to send output data due to connection recovery", ex);
            case SKIP:
                if (switchedToRecoveryState) {
                    this.recoveryFuture = this.recoveryExecutor.submit(() -> recoverProducerSession(ex, session));
                }

                return false;
        }

        return false;
    }

    private synchronized void recoverProducerSession(final Exception ex, final JmsContextWrapper session) {

        try {
            if (this.isClosed) {
                return;
            }

            final RecoveryProcessor recoveryProcessor = new RecoveryProcessor(
                    () -> {
                        if (session != null) {
                            session.close();
                        }
                    },
                    () -> {
                        final JmsContextWrapper newSession = this.connectionPool.get();
                        if (newSession == null) {
                            throw new ProfilerOutputSinkException("Unable to obtain valid session");
                        }
                    },
                    this.configuration.recoveryConfiguration().maxRetryRecoveryIntervalMs(),
                    this.configuration.recoveryConfiguration().recoveryTimeoutMs()
            );

            recoveryProcessor.recover(ex);
        } finally {
            this.inRecoveryState.compareAndSet(true, false);
        }
    }

    private Destination lookup(final String jndiName) {
        try {
            final InitialContext initialContext = new InitialContext();
            return (Destination) initialContext.lookup(jndiName);
        } catch (NamingException ex) {
            throw new ProfilerOutputSinkException(ex);
        }
    }
}
