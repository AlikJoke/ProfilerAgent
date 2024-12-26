package ru.joke.profiler.output.handlers.util.pool;

import ru.joke.profiler.output.handlers.OutputDataSink;
import ru.joke.profiler.output.handlers.ProfilerOutputSinkException;
import ru.joke.profiler.output.handlers.util.ConcurrentLinkedBlockingQueue;

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
            final ConnectionPoolConfiguration configuration) {
        this.connectionFactory = connectionFactory;
        this.configuration = configuration;
        this.pool = new ConcurrentLinkedBlockingQueue<>(configuration.maxPoolSize());
        this.registry = new ArrayList<>();
        this.idleConnectionsTerminator =
                configuration.keepAliveIdleTime() == -1
                        ? null
                        : Executors.newSingleThreadScheduledExecutor(r -> {
                            final Thread thread = new Thread(r);
                            thread.setDaemon(true);
                            thread.setName(IDLE_CONNECTIONS_TERMINATOR_THREAD_NAME);
                            thread.setUncaughtExceptionHandler((t, e) -> logger.log(Level.SEVERE, "Unable to terminate idle connections", e));

                            return thread;
                        });
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
                    this.configuration.keepAliveIdleTime(),
                    this.configuration.keepAliveIdleTime(),
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
            result = this.pool.poll(this.configuration.maxConnectionWaitTime(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Thread was interrupted", e);
            throw new ProfilerOutputSinkException(e);
        }

        if (result == null || !result.init()) {
            final String infoMsg = String.format("Can't poll valid connection from the pool in timeout %d", this.configuration.maxConnectionWaitTime());
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
        return connection.lastUsedTimestamp() + this.configuration.keepAliveIdleTime() <= currentTimestamp;
    }
}
