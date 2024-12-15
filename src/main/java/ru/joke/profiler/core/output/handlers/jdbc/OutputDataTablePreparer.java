package ru.joke.profiler.core.output.handlers.jdbc;

import ru.joke.profiler.core.output.handlers.ProfilerOutputSinkException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

final class OutputDataTablePreparer {

    private static final String CHECK_TABLE_QUERY = "SELECT 1 FROM %s WHERE 1 = 0";
    private static final String CREATE_TABLE_QUERY = "CREATE TABLE %s (%s)";
    private static final String DELETE_FROM_TABLE_QUERY = "DELETE FROM TABLE %s";
    private static final String DROP_TABLE_QUERY = "DROP TABLE %s";

    private final ConnectionFactory connectionFactory;
    private final JdbcSinkConfiguration.OutputTableConfiguration configuration;
    private final OutputDataTableSchemaValidator tableSchemaValidator;

    OutputDataTablePreparer(
            final JdbcSinkConfiguration.OutputTableConfiguration configuration,
            final ConnectionFactory connectionFactory,
            final OutputDataTableSchemaValidator tableSchemaValidator) {
        this.configuration = configuration;
        this.connectionFactory = connectionFactory;
        this.tableSchemaValidator = tableSchemaValidator;
    }

    void prepare() throws SQLException {
        try (final Connection connection = this.connectionFactory.create();
             final Statement statement = connection.createStatement()) {
            connection.setAutoCommit(true);

            final boolean isTableExists = checkTableExistence(statement);
            if (!isTableExists) {

                if (this.configuration.autoCreateTableIfNotExist()) {
                    createTable(statement);
                } else {
                    throw new ProfilerOutputSinkException("Output table does not exists");
                }

            }

            handleExistingTablePolicy(statement);

            if (!this.configuration.skipSchemaValidation()) {
                this.tableSchemaValidator.validate(statement);
            }
        }
    }

    private void handleExistingTablePolicy(final Statement statement) throws SQLException {
        switch (this.configuration.existingTablePolicy()) {
            case TRUNCATE:
                truncateTable(statement);
                return;
            case RECREATE:
                dropTable(statement);
                createTable(statement);
        }
    }

    private void createTable(final Statement statement) throws SQLException {
        final String query = buildOutputTableCreateQuery();
        statement.execute(query);
    }

    private void dropTable(final Statement statement) throws SQLException {
        final String query = String.format(DROP_TABLE_QUERY, this.configuration.tableName());
        statement.execute(query);
    }

    private void truncateTable(final Statement statement) throws SQLException {
        final String query = String.format(DELETE_FROM_TABLE_QUERY, this.configuration.tableName());
        statement.execute(query);
    }

    private boolean checkTableExistence(final Statement statement) {
        final String checkQuery = String.format(CHECK_TABLE_QUERY, this.configuration.tableName());
        try {
            statement.execute(checkQuery);
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    private String buildOutputTableCreateQuery() {
        final String columnsQueryPart =
                this.configuration.columnsMetadata()
                                    .values()
                                    .stream()
                                    .map(cm -> cm.columnName() + " " + cm.columnType())
                                    .collect(Collectors.joining(", "));
        return String.format(CREATE_TABLE_QUERY, this.configuration.tableName(), columnsQueryPart);
    }
}
