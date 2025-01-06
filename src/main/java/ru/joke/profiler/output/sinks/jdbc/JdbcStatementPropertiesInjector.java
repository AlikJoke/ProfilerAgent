package ru.joke.profiler.output.sinks.jdbc;

import ru.joke.profiler.output.sinks.util.injectors.OutputPropertiesInjector;
import ru.joke.profiler.output.sinks.ProfilerOutputSinkException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

final class JdbcStatementPropertiesInjector extends OutputPropertiesInjector<PreparedStatement> {

    JdbcStatementPropertiesInjector(final JdbcSinkConfiguration.OutputTableConfiguration configuration) {
        super(configuration.columnsMetadata().keySet());
    }

    @Override
    protected PreparedStatement injectMethodName(
            final PreparedStatement template,
            final String method,
            final String property,
            final int propertyIndex
    ) {
        return injectStringParam(template, propertyIndex, method);
    }

    @Override
    protected PreparedStatement injectMethodEnterTimestamp(
            final PreparedStatement template,
            final long methodEnterTimestamp,
            final String property,
            int propertyIndex
    ) {
        return injectLongParam(template, propertyIndex, methodEnterTimestamp);
    }

    @Override
    protected PreparedStatement injectMethodElapsedTime(
            final PreparedStatement template,
            final long methodElapsedTime,
            final String property,
            int propertyIndex
    ) {
        return injectLongParam(template, propertyIndex, methodElapsedTime);
    }

    @Override
    protected PreparedStatement injectTraceId(
            final PreparedStatement template,
            final String traceId,
            final String property,
            final int propertyIndex
    ) {
        return injectStringParam(template, propertyIndex, traceId);
    }

    @Override
    protected PreparedStatement injectDepth(
            final PreparedStatement template,
            final int depth,
            final String property,
            final int propertyIndex
    ) {
        return injectIntParam(template, propertyIndex, depth);
    }

    @Override
    protected PreparedStatement injectIp(
            final PreparedStatement template,
            final String ip,
            final String property,
            final int propertyIndex
    ) {
        return injectStringParam(template, propertyIndex, ip);
    }

    @Override
    protected PreparedStatement injectHost(
            final PreparedStatement template,
            final String host,
            final String property,
            final int propertyIndex
    ) {
        return injectStringParam(template, propertyIndex, host);
    }

    @Override
    protected PreparedStatement injectSource(
            final PreparedStatement template,
            final String source,
            final String property,
            final int propertyIndex
    ) {
        return injectStringParam(template, propertyIndex, source);
    }

    @Override
    protected PreparedStatement injectSystemProperty(
            final PreparedStatement template,
            final String value,
            final String property,
            final int propertyIndex
    ) {
        return injectStringParam(template, propertyIndex, value);
    }

    @Override
    protected PreparedStatement injectThreadName(
            final PreparedStatement template,
            final String threadName,
            final String property,
            final int propertyIndex
    ) {
        return injectStringParam(template, propertyIndex, threadName);
    }

    @Override
    protected PreparedStatement injectCurrentTimestamp(
            final PreparedStatement template,
            final LocalDateTime timestamp,
            final String property,
            final int propertyIndex
    ) {
        try {
            if (timestamp == null) {
                template.setNull(propertyIndex, Types.TIMESTAMP);
            } else {
                template.setTimestamp(propertyIndex, Timestamp.valueOf(timestamp));
            }

            return template;
        } catch (SQLException e) {
            throw new ProfilerOutputSinkException(e);
        }
    }

    private PreparedStatement injectStringParam(
            final PreparedStatement statement,
            final int index,
            final String param
    ) {
        try {
            if (param == null) {
                statement.setNull(index, Types.VARCHAR);
            } else {
                statement.setString(index, param);
            }

            return statement;
        } catch (SQLException ex) {
            throw new ProfilerOutputSinkException(ex);
        }
    }

    private PreparedStatement injectLongParam(
            final PreparedStatement statement,
            final int index,
            final long param
    ) {
        try {
            statement.setLong(index, param);
            return statement;
        } catch (SQLException ex) {
            throw new ProfilerOutputSinkException(ex);
        }
    }

    private PreparedStatement injectIntParam(
            final PreparedStatement statement,
            final int index,
            final int param
    ) {
        try {
            statement.setInt(index, param);
            return statement;
        } catch (SQLException ex) {
            throw new ProfilerOutputSinkException(ex);
        }
    }
}
