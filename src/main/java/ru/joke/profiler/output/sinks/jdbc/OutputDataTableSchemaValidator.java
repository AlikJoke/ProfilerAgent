package ru.joke.profiler.output.sinks.jdbc;

import ru.joke.profiler.output.sinks.ProfilerOutputSinkException;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

final class OutputDataTableSchemaValidator {

    private static final String CHECK_COLUMN_QUERY = "SELECT %s FROM %s WHERE 1 = 0";

    private final JdbcSinkConfiguration.OutputTableConfiguration configuration;

    OutputDataTableSchemaValidator(final JdbcSinkConfiguration.OutputTableConfiguration configuration) {
        this.configuration = checkNotNull(configuration, "configuration");
    }

    void validate(final Statement statement) {
        final String columns =
                this.configuration.columnsMetadata()
                                    .values()
                                    .stream()
                                    .map(JdbcSinkConfiguration.OutputTableConfiguration.ColumnMetadata::columnName)
                                    .collect(Collectors.joining(","));

        final String validationQuery = String.format(CHECK_COLUMN_QUERY, columns, this.configuration.tableName());
        try {
            statement.execute(validationQuery);
        } catch (SQLException ex) {
            throw new ProfilerOutputSinkException("Columns in the table does not match to the columns from the metadata", ex);
        }
    }
}
