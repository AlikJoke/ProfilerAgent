package ru.joke.profiler.output.sinks.util.pool;

import ru.joke.profiler.output.sinks.OutputDataSink;
import ru.joke.profiler.output.sinks.ProfilerOutputSinkException;
import ru.joke.profiler.output.sinks.util.ConcurrentLinkedBlockingQueue;
import ru.joke.profiler.util.ProfilerThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class StdConnectionPool<T extends PooledConnection> implements ConnectionPool<T> {

    private static final Logger logger = Logger.getLogger(OutputDataSink.class.getCanonicalName());

    private static final String IDLE_CONNECTIONS_TERMINATOR_THREAD_NAME = "profiler-idle-connections-terminator";

    private final ConnectionFactory<T> connectionFactory;
    private final ConnectionPoolConfiguration configuration;
    private final ConcurrentLinkedBlockingQueue<T> pool;
    private final ScheduledExecutorService idleConnectionsTerminator;
    private final List<T> registry;

    StdConnectionPool(
            final ConnectionFactory<T> connectionFactory,
            final ConnectionPoolConfiguration configuration
    ) {
        this.connectionFactory = connectionFactory;
        this.configuration = configuration;
        this.pool = new ConcurrentLinkedBlockingQueue<>(configuration.maxPoolSize());
        this.registry = new ArrayList<>();
        this.idleConnectionsTerminator =
                configuration.keepAliveIdleMs() == -1
                        ? null
                        : Executors.newSingleThreadScheduledExecutor(new ProfilerThreadFactory(IDLE_CONNECTIONS_TERMINATOR_THREAD_NAME, false));
    }

    @Override
    public void init() {
        for (int i = 0; i < this.configuration.maxPoolSize(); i++) {
            final T connectionWrapper = this.connectionFactory.create();
            if (i < this.configuration.initialPoolSize()) {
                connectionWrapper.init();
            }

            this.pool.offer(connectionWrapper);
        }

        this.pool.forEach(this.registry::add);

        if (this.idleConnectionsTerminator != null) {
            this.idleConnectionsTerminator.scheduleWithFixedDelay(
                    this::terminateExpiredIdleConnections,
                    this.configuration.keepAliveIdleMs(),
                    this.configuration.keepAliveIdleMs(),
                    TimeUnit.MILLISECONDS
            );
        }

    }

    @Override
    public void release(final T connection) {
        connection.onRelease();
        this.pool.offer(connection);
    }

    @Override
    public T get() {
        final T result;
        try {
            result = this.pool.poll(this.configuration.maxConnectionWaitMs(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Thread was interrupted", e);
            throw new ProfilerOutputSinkException(e);
        }

        if (result == null || !result.init()) {
            final String infoMsg = String.format("Can't poll valid connection from the pool in timeout %d", this.configuration.maxConnectionWaitMs());
            if (this.configuration.connectionUnavailabilityPolicy() == ConnectionPoolConfiguration.ConnectionUnavailabilityPolicy.ERROR) {
                throw new ProfilerOutputSinkException(infoMsg);
            }

            logger.warning(infoMsg);

            return null;
        }

        return result;
    }

    @Override
    public synchronized void close() {
        if (this.idleConnectionsTerminator != null) {
            this.idleConnectionsTerminator.shutdown();
        }

        this.registry.forEach(PooledConnection::close);
    }

    private void terminateExpiredIdleConnections() {
        final long currentTimestamp = System.currentTimeMillis();
        this.pool.forEach(wrapper -> {
            if (isConnectionExpired(wrapper, currentTimestamp)) {
                synchronized (wrapper) {
                    if (isConnectionExpired(wrapper, currentTimestamp)) {
                        wrapper.close();
                    }
                }
            }
        });
    }

    private boolean isConnectionExpired(final T connection, final long currentTimestamp) {
        return connection.lastUsedTimestamp() + this.configuration.keepAliveIdleMs() <= currentTimestamp;
    }
}
