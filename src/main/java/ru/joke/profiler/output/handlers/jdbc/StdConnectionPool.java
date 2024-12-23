package ru.joke.profiler.output.handlers.jdbc;

import ru.joke.profiler.output.handlers.OutputDataSink;
import ru.joke.profiler.output.handlers.ProfilerOutputSinkException;
import ru.joke.profiler.output.handlers.util.ConcurrentLinkedBlockingQueue;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

final class StdConnectionPool implements ConnectionPool {

    private static final Logger logger = Logger.getLogger(OutputDataSink.class.getCanonicalName());

    private static final String IDLE_CONNECTIONS_TERMINATOR_THREAD_NAME = "profiler-idle-connections-terminator";

    private final ConnectionFactory connectionFactory;
    private final JdbcSinkConfiguration.ConnectionPoolConfiguration configuration;
    private final ConcurrentLinkedBlockingQueue<ConnectionWrapper> pool;
    private final ScheduledExecutorService idleConnectionsTerminator;
    private final List<ConnectionWrapper> registry;

    StdConnectionPool(
            final ConnectionFactory connectionFactory,
            final JdbcSinkConfiguration.ConnectionPoolConfiguration poolConfiguration) {
        this.connectionFactory = connectionFactory;
        this.configuration = poolConfiguration;
        this.pool = new ConcurrentLinkedBlockingQueue<>(poolConfiguration.maxPoolSize());
        this.registry = new ArrayList<>();
        this.idleConnectionsTerminator =
                poolConfiguration.keepAliveIdleTime() == -1
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
            this.pool.offer(new ConnectionWrapper(this.connectionFactory::create, i < this.configuration.initialPoolSize()));
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
    public void release(final Connection connection) {
        if (connection instanceof ConnectionWrapper) {
            final ConnectionWrapper wrapper = (ConnectionWrapper) connection;
            wrapper.onRelease();

            this.pool.offer(wrapper);
        } else {
            throw new ClassCastException("Unsupported type of connection");
        }
    }

    @Override
    public Connection get() {
        final ConnectionWrapper result;
        try {
            result = this.pool.poll(this.configuration.maxConnectionWaitTime(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Thread was interrupted", e);
            throw new ProfilerOutputSinkException(e);
        }

        if (result == null) {
            throw new ProfilerOutputSinkException("Can't poll connection from the pool in timeout " + this.configuration.maxConnectionWaitTime());
        }

        result.init();
        return result;
    }

    @Override
    public synchronized void close() {
        if (this.idleConnectionsTerminator != null) {
            this.idleConnectionsTerminator.shutdown();
        }

        this.registry.forEach(ConnectionWrapper::close);
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

    private boolean isConnectionExpired(final ConnectionWrapper connection, final long currentTimestamp) {
        return connection.lastUsedTimestamp + this.configuration.keepAliveIdleTime() <= currentTimestamp;
    }

    private static class ConnectionWrapper implements Connection {

        private final Supplier<Connection> connectionRetriever;
        private volatile Connection connection;
        private volatile long lastUsedTimestamp;
        private volatile boolean isClosed;

        private ConnectionWrapper(final Supplier<Connection> connectionRetriever, final boolean eagerCreation) {
            this.connectionRetriever = connectionRetriever;
            if (eagerCreation) {
                init();
            }
        }

        private void onRelease() {
            this.lastUsedTimestamp = System.currentTimeMillis();
        }

        private synchronized void init() {
            try {
                if (this.isClosed) {
                    throw new ProfilerOutputSinkException("Sink already closed");
                }

                if (this.connection == null || this.connection.isClosed()) {
                    this.connection = connectionRetriever.get();
                    this.lastUsedTimestamp = System.currentTimeMillis();
                }
            } catch (SQLException e) {
                throw new ProfilerOutputSinkException(e);
            }
        }

        @Override
        public Statement createStatement() throws SQLException {
            return connection.createStatement();
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return connection.prepareStatement(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            return connection.prepareCall(sql);
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            return connection.nativeSQL(sql);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            connection.setAutoCommit(autoCommit);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return connection.getAutoCommit();
        }

        @Override
        public void commit() throws SQLException {
            connection.commit();
        }

        @Override
        public void rollback() throws SQLException {
            connection.rollback();
        }

        @Override
        public synchronized void close() {
            this.isClosed = true;
            if (this.connection == null) {
                return;
            }

            try {
                this.connection.close();
                this.connection = null;
            } catch (SQLException e) {
                throw new ProfilerOutputSinkException(e);
            }
        }

        @Override
        public boolean isClosed() throws SQLException {
            return connection == null || connection.isClosed();
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return connection.getMetaData();
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            connection.setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return connection.isReadOnly();
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {
            connection.setCatalog(catalog);
        }

        @Override
        public String getCatalog() throws SQLException {
            return connection.getCatalog();
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            connection.setTransactionIsolation(level);
        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return connection.getTransactionIsolation();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return connection.getWarnings();
        }

        @Override
        public void clearWarnings() throws SQLException {
            connection.clearWarnings();
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return connection.createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return connection.getTypeMap();
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
            connection.setTypeMap(map);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {
            connection.setHoldability(holdability);
        }

        @Override
        public int getHoldability() throws SQLException {
            return connection.getHoldability();
        }

        @Override
        public Savepoint setSavepoint() throws SQLException {
            return connection.setSavepoint();
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            return connection.setSavepoint(name);
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {
            connection.rollback(savepoint);
        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            connection.releaseSavepoint(savepoint);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
                throws SQLException {
            return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
                throws SQLException {
            return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return connection.prepareStatement(sql, autoGeneratedKeys);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return connection.prepareStatement(sql, columnIndexes);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return connection.prepareStatement(sql, columnNames);
        }

        @Override
        public Clob createClob() throws SQLException {
            return connection.createClob();
        }

        @Override
        public Blob createBlob() throws SQLException {
            return connection.createBlob();
        }

        @Override
        public NClob createNClob() throws SQLException {
            return connection.createNClob();
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            return connection.createSQLXML();
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return connection.isValid(timeout);
        }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {
            connection.setClientInfo(name, value);
        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {
            connection.setClientInfo(properties);
        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return connection.getClientInfo(name);
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return connection.getClientInfo();
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return connection.createArrayOf(typeName, elements);
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return connection.createStruct(typeName, attributes);
        }

        @Override
        public void setSchema(String schema) throws SQLException {
            connection.setSchema(schema);
        }

        @Override
        public String getSchema() throws SQLException {
            return connection.getSchema();
        }

        @Override
        public void abort(Executor executor) throws SQLException {
            connection.abort(executor);
        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
            connection.setNetworkTimeout(executor, milliseconds);
        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return connection.getNetworkTimeout();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return connection.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return connection.isWrapperFor(iface);
        }
    }
}
