package ru.joke.profiler.transformation.spy.jdbc;

import ru.joke.profiler.output.ExecutionTimeRegistrar;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class StatementSpy<T extends Statement> implements Statement {

    protected final T delegate;
    protected final String statementType;
    protected final StringBuilder methodInfoBuilder;
    protected final ExecutionTimeRegistrar registrar;
    protected final JdbcSpyConfiguration configuration;
    protected List<String> batchQueries;

    StatementSpy(
            final T delegate,
            final ExecutionTimeRegistrar registrar,
            final JdbcSpyConfiguration configuration
    ) {
        this.delegate = delegate;
        this.statementType = delegate.getClass().getCanonicalName();
        this.methodInfoBuilder = new StringBuilder(1024);
        this.registrar = registrar;
        this.configuration = configuration;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        final String methodInfo = composeMethodInfo("executeQuery", sql);
        this.registrar.registerMethodEnter(methodInfo);

        final long startTime = System.nanoTime();
        try {
            return this.delegate.executeQuery(sql);
        } finally {
            registerElapsedTime(methodInfo, startTime, System.nanoTime());
        }
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return executeUpdateQuery(() -> this.delegate.executeUpdate(sql), sql);
    }

    @Override
    public void close() throws SQLException {
        delegate.close();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return delegate.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        delegate.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return delegate.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        delegate.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        delegate.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return delegate.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        delegate.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        delegate.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        delegate.setCursorName(name);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return executeQuery(() -> this.delegate.execute(sql), sql);
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return delegate.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return delegate.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return delegate.getMoreResults();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        delegate.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return delegate.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        delegate.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return delegate.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return delegate.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return delegate.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        delegate.addBatch(sql);
        if (batchQueries == null) {
            batchQueries = new ArrayList<>();
        }

        batchQueries.add(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        delegate.clearBatch();
        if (batchQueries != null) {
            batchQueries.clear();
        }
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return executeBatchQuery(this.delegate::executeBatch, "executeBatch");
    }

    @Override
    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return delegate.getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return delegate.getGeneratedKeys();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return executeUpdateQuery(() -> delegate.executeUpdate(sql, autoGeneratedKeys), sql);
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return executeUpdateQuery(() -> delegate.executeUpdate(sql, columnIndexes), sql);
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return executeUpdateQuery(() -> delegate.executeUpdate(sql, columnNames), sql);
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return executeQuery(() -> delegate.execute(sql, autoGeneratedKeys), sql);
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return executeQuery(() -> delegate.execute(sql, columnIndexes), sql);
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return executeQuery(() -> delegate.execute(sql, columnNames), sql);
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return delegate.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        delegate.setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return delegate.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        delegate.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return delegate.isCloseOnCompletion();
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        return delegate.getLargeUpdateCount();
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        delegate.setLargeMaxRows(max);
    }

    @Override
    public long getLargeMaxRows() throws SQLException {
        return delegate.getLargeMaxRows();
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        return executeBatchQuery(this.delegate::executeLargeBatch, "executeLargeBatch");
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
        return executeLargeUpdateQuery(() -> delegate.executeLargeUpdate(sql), sql);
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return executeLargeUpdateQuery(() -> delegate.executeLargeUpdate(sql, autoGeneratedKeys), sql);
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return executeLargeUpdateQuery(() -> delegate.executeLargeUpdate(sql, columnIndexes), sql);
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        return executeLargeUpdateQuery(() -> delegate.executeLargeUpdate(sql, columnNames), sql);
    }

    @Override
    public <K> K unwrap(Class<K> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    protected int executeUpdateQuery(
            final ExecutableUpdateSqlQuery updateSqlQuery,
            final String sql
    ) throws SQLException {
        final String methodInfo = composeMethodInfo("executeUpdate", sql);
        this.registrar.registerMethodEnter(methodInfo);

        final long startTime = System.nanoTime();
        try {
            return updateSqlQuery.execute();
        } finally {
            registerElapsedTime(methodInfo, startTime, System.nanoTime());
        }
    }

    protected long executeLargeUpdateQuery(
            final LargeExecutableUpdateSqlQuery updateSqlQuery,
            final String sql
    ) throws SQLException {
        final String methodInfo = composeMethodInfo("executeLargeUpdate", sql);
        this.registrar.registerMethodEnter(methodInfo);

        final long startTime = System.nanoTime();
        try {
            return updateSqlQuery.execute();
        } finally {
            registerElapsedTime(methodInfo, startTime, System.nanoTime());
        }
    }

    protected boolean executeQuery(
            final ExecutableSqlQuery query,
            final String sql
    ) throws SQLException {
        final String methodInfo = composeMethodInfo("execute", sql);
        this.registrar.registerMethodEnter(methodInfo);

        final long startTime = System.nanoTime();
        try {
            return query.execute();
        } finally {
            registerElapsedTime(methodInfo, startTime, System.nanoTime());
        }
    }

    protected <S> S executeBatchQuery(
            final ExecutableBatchSqlQuery<S> query,
            final String methodName
    ) throws SQLException {
        final String methodInfo = composeBatchMethodInfo(
                methodName,
                this.batchQueries
        );

        this.registrar.registerMethodEnter(methodInfo);
        final long startTime = System.nanoTime();
        try {
            return query.execute();
        } finally {
            registerElapsedTime(methodInfo, startTime, System.nanoTime());
            if (this.batchQueries != null) {
                this.batchQueries.clear();
            }
        }
    }

    protected String composeMethodInfo(
            final String methodName,
            final String sql
    ) {
        final String result =
                this.methodInfoBuilder
                        .append(this.statementType)
                        .append('.')
                        .append(methodName)
                        .append('(')
                        .append(sql)
                        .append(')')
                        .toString();
        this.methodInfoBuilder.delete(0, this.methodInfoBuilder.length());

        return result;
    }

    protected String composeBatchMethodInfo(
            final String methodName,
            final List<String> sql
    ) {
        if (sql == null) {
            return composeMethodInfo(methodName, "<no queries>");
        }

        this.methodInfoBuilder
                .append(this.statementType)
                .append('.')
                .append(methodName)
                .append('(');

        final int lastIndex = sql.size() - 1;
        for (int i = 0; i < sql.size(); i++) {
            final String query = sql.get(i);
            this.methodInfoBuilder.append(query);

            if (lastIndex != i) {
                this.methodInfoBuilder.append(';');
            }
        }

        final String result = this.methodInfoBuilder.append(')').toString();

        this.methodInfoBuilder.delete(0, this.methodInfoBuilder.length());

        return result;
    }

    protected void registerElapsedTime(
            final String methodInfo,
            final long startTime,
            final long finishTime
    ) {
        final long elapsedTime;
        if ((elapsedTime = finishTime - startTime) >= this.configuration.minExecutionThresholdNs()) {
            this.registrar.registerMethodExit(methodInfo, startTime, elapsedTime);
        } else {
            this.registrar.registerMethodExit();
        }
    }

    @FunctionalInterface
    interface ExecutableBatchSqlQuery<T> {

        T execute() throws SQLException;
    }

    @FunctionalInterface
    interface ExecutableUpdateSqlQuery {

        int execute() throws SQLException;
    }

    @FunctionalInterface
    interface LargeExecutableUpdateSqlQuery {

        long execute() throws SQLException;
    }

    @FunctionalInterface
    interface ExecutableSqlQuery {

        boolean execute() throws SQLException;
    }
}
