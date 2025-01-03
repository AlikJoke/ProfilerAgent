package ru.joke.profiler.transformation.spy.jdbc;

import ru.joke.profiler.output.ExecutionTimeRegistrar;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

class PreparedStatementSpy<T extends PreparedStatement> extends StatementSpy<T> implements PreparedStatement {

    protected static final String ASCII_STREAM = "<ascii>";
    protected static final String BINARY_STREAM = "<b>";
    protected static final String CHARACTER_STREAM = "<cs>";
    protected static final String NCHARACTER_STREAM = "<ncs>";
    protected static final String CLOB = "<clob>";
    protected static final String NCLOB = "<nclob>";
    protected static final String BLOB = "<blob>";
    protected static final String UNICODE_STREAM = "<us>";

    protected final String preparedQuery;
    protected int batchCount;
    protected StringBuilder parametersBuilder;

    PreparedStatementSpy(
            final T delegate,
            final ExecutionTimeRegistrar registrar,
            final JdbcSpyConfiguration configuration,
            final String preparedQuery
    ) {
        super(delegate, registrar, configuration);
        this.preparedQuery = preparedQuery;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        final String methodInfo = composeMethodInfo("executeQuery", this.preparedQuery);
        this.registrar.registerMethodEnter(methodInfo);

        final long startTime = System.nanoTime();
        try {
            return this.delegate.executeQuery();
        } finally {
            registerElapsedTime(methodInfo, startTime, System.nanoTime());
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        return executeUpdateQuery(this.delegate::executeUpdate, this.preparedQuery);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        delegate.setNull(parameterIndex, sqlType);
        appendParameterInfo(parameterIndex, null);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        delegate.setBoolean(parameterIndex, x);
        if (this.configuration.printQueryParameters()) {
            appendParameterInfo(parameterIndex, String.valueOf(x));
        }
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        delegate.setByte(parameterIndex, x);
        if (this.configuration.printQueryParameters()) {
            appendParameterInfo(parameterIndex, String.valueOf(x));
        }
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        delegate.setShort(parameterIndex, x);
        if (this.configuration.printQueryParameters()) {
            appendParameterInfo(parameterIndex, String.valueOf(x));
        }
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        delegate.setInt(parameterIndex, x);
        if (this.configuration.printQueryParameters()) {
            appendParameterInfo(parameterIndex, String.valueOf(x));
        }
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        delegate.setLong(parameterIndex, x);
        if (this.configuration.printQueryParameters()) {
            appendParameterInfo(parameterIndex, String.valueOf(x));
        }
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        delegate.setFloat(parameterIndex, x);
        if (this.configuration.printQueryParameters()) {
            appendParameterInfo(parameterIndex, String.valueOf(x));
        }
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        delegate.setDouble(parameterIndex, x);
        if (this.configuration.printQueryParameters()) {
            appendParameterInfo(parameterIndex, String.valueOf(x));
        }
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        delegate.setBigDecimal(parameterIndex, x);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        delegate.setString(parameterIndex, x);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        delegate.setBytes(parameterIndex, x);
        if (this.configuration.printQueryParameters()) {
            appendParameterInfo(parameterIndex, Arrays.toString(x));
        }
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        delegate.setDate(parameterIndex, x);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        delegate.setTime(parameterIndex, x);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        delegate.setTimestamp(parameterIndex, x);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        delegate.setAsciiStream(parameterIndex, x, length);
        appendParameterInfo(parameterIndex, ASCII_STREAM);
    }

    @Override
    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        delegate.setUnicodeStream(parameterIndex, x, length);
        appendParameterInfo(parameterIndex, UNICODE_STREAM);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        delegate.setBinaryStream(parameterIndex, x, length);
        appendParameterInfo(parameterIndex, BINARY_STREAM);
    }

    @Override
    public void clearParameters() throws SQLException {
        delegate.clearParameters();
        this.parametersBuilder.delete(0, this.parametersBuilder.length());
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        delegate.setObject(parameterIndex, x, targetSqlType);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        delegate.setObject(parameterIndex, x);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public boolean execute() throws SQLException {
        return executeQuery(this.delegate::execute, this.preparedQuery);
    }

    @Override
    public void addBatch() throws SQLException {
        delegate.addBatch();
        this.batchCount++;
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        delegate.setCharacterStream(parameterIndex, reader, length);
        appendParameterInfo(parameterIndex, CHARACTER_STREAM);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        delegate.setRef(parameterIndex, x);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        delegate.setBlob(parameterIndex, x);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        delegate.setClob(parameterIndex, x);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        delegate.setArray(parameterIndex, x);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        delegate.setDate(parameterIndex, x, cal);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        delegate.setTime(parameterIndex, x, cal);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        delegate.setTimestamp(parameterIndex, x, cal);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        delegate.setNull(parameterIndex, sqlType, typeName);
        appendParameterInfo(parameterIndex, null);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        delegate.setURL(parameterIndex, x);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return delegate.getParameterMetaData();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        delegate.setRowId(parameterIndex, x);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        delegate.setNString(parameterIndex, value);
        appendParameterInfo(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        delegate.setNCharacterStream(parameterIndex, value, length);
        appendParameterInfo(parameterIndex, NCHARACTER_STREAM);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        delegate.setNClob(parameterIndex, value);
        appendParameterInfo(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        delegate.setClob(parameterIndex, reader, length);
        appendParameterInfo(parameterIndex, CLOB);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        delegate.setBlob(parameterIndex, inputStream, length);
        appendParameterInfo(parameterIndex, BLOB);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        delegate.setNClob(parameterIndex, reader, length);
        appendParameterInfo(parameterIndex, CLOB);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        delegate.setSQLXML(parameterIndex, xmlObject);
        appendParameterInfo(parameterIndex, xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        delegate.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        delegate.setAsciiStream(parameterIndex, x, length);
        appendParameterInfo(parameterIndex, ASCII_STREAM);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        delegate.setBinaryStream(parameterIndex, x, length);
        appendParameterInfo(parameterIndex, BINARY_STREAM);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        delegate.setCharacterStream(parameterIndex, reader, length);
        appendParameterInfo(parameterIndex, CHARACTER_STREAM);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        delegate.setAsciiStream(parameterIndex, x);
        appendParameterInfo(parameterIndex, ASCII_STREAM);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        delegate.setBinaryStream(parameterIndex, x);
        appendParameterInfo(parameterIndex, BINARY_STREAM);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        delegate.setCharacterStream(parameterIndex, reader);
        appendParameterInfo(parameterIndex, CHARACTER_STREAM);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        delegate.setNCharacterStream(parameterIndex, value);
        appendParameterInfo(parameterIndex, NCHARACTER_STREAM);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        delegate.setClob(parameterIndex, reader);
        appendParameterInfo(parameterIndex, CLOB);
    }

    @Override
    public void clearBatch() throws SQLException {
        super.clearBatch();
        this.batchCount = 0;
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        delegate.setBlob(parameterIndex, inputStream);
        appendParameterInfo(parameterIndex, BLOB);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        delegate.setNClob(parameterIndex, reader);
        appendParameterInfo(parameterIndex, NCLOB);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        delegate.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        delegate.setObject(parameterIndex, x, targetSqlType);
        appendParameterInfo(parameterIndex, x);
    }

    @Override
    public long executeLargeUpdate() throws SQLException {
        return executeLargeUpdateQuery(this.delegate::executeLargeUpdate, this.preparedQuery);
    }

    @Override
    public int[] executeBatch() throws SQLException {
        final int[] result = super.executeBatch();
        this.batchCount = 0;
        return result;
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        final long[] result = super.executeLargeBatch();
        this.batchCount = 0;
        return result;
    }

    @Override
    protected String composeBatchMethodInfo(
            final String methodName,
            final List<String> sql
    ) {
        final String batchPreparedStatementInfo = this.batchCount == 0 ? null : "(" + this.batchCount + " times) " + this.preparedQuery;
        if (sql != null && !sql.isEmpty() && batchPreparedStatementInfo != null) {
            sql.add(batchPreparedStatementInfo);
            return super.composeBatchMethodInfo(methodName, sql, buildParametersFinalInfo());
        } else {
            return composeMethodInfo(methodName, batchPreparedStatementInfo == null ? "" : batchPreparedStatementInfo);
        }
    }

    @Override
    protected String composeMethodInfo(
            final String methodName,
            final String sql
    ) {
        return super.composeMethodInfo(
                methodName,
                sql,
                buildParametersFinalInfo()
        );
    }

    protected void appendParameterInfo(final int index, final Object value) {
        appendParameterInfo(String.valueOf(index), value);
    }

    protected void appendParameterInfo(final String paramId, final Object value) {
        if (!this.configuration.printQueryParameters()) {
            return;
        }

        if (this.parametersBuilder == null) {
            this.parametersBuilder = new StringBuilder(1024);
        }

        this.parametersBuilder
                .append('$')
                .append(paramId)
                .append('=')
                .append('\'')
                .append(value)
                .append('\'')
                .append(this.configuration.queryParametersDelimiter());
    }

    private String buildParametersFinalInfo() {
        return this.parametersBuilder == null ? null : this.parametersBuilder.deleteCharAt(this.parametersBuilder.length() - 1).toString();
    }
}
