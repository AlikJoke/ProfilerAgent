package ru.joke.profiler.core.output.handlers.jdbc;

import ru.joke.profiler.core.configuration.InvalidConfigurationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static ru.joke.profiler.core.configuration.ConfigurationProperties.*;

final class JdbcSinkConfigurationLoader {

    private static final int DEFAULT_MAX_POOL_SIZE = 32;
    private static final int DEFAULT_INIT_POOL_SIZE = 4;
    private static final long DEFAULT_KEEP_ALIVE_IDLE_TIME = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
    private static final long DEFAULT_MAX_WAIT_TIME = TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS);

    JdbcSinkConfiguration load(final Map<String, String> properties) {
        final JdbcSinkConfiguration.OutputDataInsertionConfiguration insertionConfiguration = loadInsertionConfiguration(properties);
        final JdbcSinkConfiguration.ConnectionFactoryConfiguration connectionFactoryConfiguration = loadConnectionFactoryConfiguration(properties);
        final JdbcSinkConfiguration.ConnectionPoolConfiguration poolConfiguration = loadConnectionPoolConfiguration(properties);
        final JdbcSinkConfiguration.OutputTableConfiguration outputTableConfiguration = loadOutputTableConfiguration(properties);

        return new JdbcSinkConfiguration(
                insertionConfiguration,
                outputTableConfiguration,
                poolConfiguration,
                connectionFactoryConfiguration
        );
    }

    private JdbcSinkConfiguration.OutputDataInsertionConfiguration loadInsertionConfiguration(final Map<String, String> properties) {
        final boolean enableBatching = parseBooleanProperty(properties, STATIC_JDBC_SINK_ENABLE_BATCHING);
        final int batchSize = parseIntProperty(properties, STATIC_JDBC_SINK_BATCH_SIZE, Integer.MAX_VALUE);

        return new JdbcSinkConfiguration.OutputDataInsertionConfiguration(enableBatching, batchSize);
    }

    private JdbcSinkConfiguration.OutputTableConfiguration loadOutputTableConfiguration(final Map<String, String> properties) {
        final String tableName = findRequiredProperty(properties, STATIC_JDBC_SINK_OUTPUT_TABLE_NAME);
        final String existingTablePolicyStr = properties.get(STATIC_JDBC_SINK_EXISTING_TABLE_POLICY);
        final ExistingTablePolicy existingTablePolicy = ExistingTablePolicy.parse(existingTablePolicyStr);
        final boolean autoCreateTable = properties.getOrDefault(STATIC_JDBC_SINK_AUTO_CREATE_OUTPUT_TABLE, "").isEmpty() || parseBooleanProperty(properties, STATIC_JDBC_SINK_AUTO_CREATE_OUTPUT_TABLE);
        final boolean skipSchemaValidation = parseBooleanProperty(properties, STATIC_JDBC_SINK_SKIP_SCHEMA_VALIDATION);

        final String columnsMetadataStr = findRequiredProperty(properties, STATIC_JDBC_SINK_COLUMNS_METADATA);
        final Map<String, JdbcSinkConfiguration.OutputTableConfiguration.ColumnMetadata> columnsMetadata = parseColumnsMetadata(columnsMetadataStr);

        return new JdbcSinkConfiguration.OutputTableConfiguration(
                tableName,
                existingTablePolicy,
                autoCreateTable,
                skipSchemaValidation,
                columnsMetadata
        );
    }

    private Map<String, JdbcSinkConfiguration.OutputTableConfiguration.ColumnMetadata> parseColumnsMetadata(final String metadata) {
        final String[] columnsMetadataParts = metadata.split(";");
        final Map<String, JdbcSinkConfiguration.OutputTableConfiguration.ColumnMetadata> result = new LinkedHashMap<>();
        for (final String columnMetadata : columnsMetadataParts) {
            if (columnMetadata == null || columnMetadata.isEmpty()) {
                throw new InvalidConfigurationException("Column metadata shouldn't be empty");
            }

            final String[] columnParts = columnMetadata.split(":");
            if (columnParts.length > 3 || columnParts.length < 2) {
                throw new InvalidConfigurationException("Column metadata must contain 2 or 3 parts separated by the colon: " + columnMetadata);
            }

            final String property = columnParts[0];
            final String columnNamePart = columnParts[columnParts.length - 2];
            final String column = columnNamePart.isEmpty() ? property : columnNamePart;
            final String type = columnParts[columnParts.length - 1];

            if (property.isEmpty() || type.isEmpty()) {
                throw new InvalidConfigurationException("Property or column type is not set: " + columnMetadata);
            }

            result.put(property, new JdbcSinkConfiguration.OutputTableConfiguration.ColumnMetadata(column, type));
        }

        return result;
    }

    private JdbcSinkConfiguration.ConnectionPoolConfiguration loadConnectionPoolConfiguration(final Map<String, String> properties) {
        final boolean enablePooling = parseBooleanProperty(properties, STATIC_JDBC_SINK_CONNECTION_POOL_ENABLED);
        final int maxPoolSize = parseIntProperty(properties, STATIC_JDBC_SINK_CONNECTION_POOL_MAX_POOL, DEFAULT_MAX_POOL_SIZE);
        final int initialPoolSize = parseIntProperty(properties, STATIC_JDBC_SINK_CONNECTION_POOL_INIT_POOL, DEFAULT_INIT_POOL_SIZE);
        final long keepAliveIdleTime = parseLongProperty(properties, STATIC_JDBC_SINK_CONNECTION_POOL_KEEP_ALIVE_IDLE, DEFAULT_KEEP_ALIVE_IDLE_TIME);
        final long maxWaitTime = parseLongProperty(properties, STATIC_JDBC_SINK_CONNECTION_POOL_MAX_WAIT, DEFAULT_MAX_WAIT_TIME);

        return new JdbcSinkConfiguration.ConnectionPoolConfiguration(
                enablePooling,
                maxPoolSize,
                initialPoolSize,
                keepAliveIdleTime,
                maxWaitTime
        );
    }

    private JdbcSinkConfiguration.ConnectionFactoryConfiguration loadConnectionFactoryConfiguration(final Map<String, String> properties) {
        final String url = findRequiredProperty(properties, STATIC_JDBC_SINK_CONNECTION_FACTORY_URL);
        final Properties connectionProperties = new Properties();
        properties.forEach((p, v) -> {
            if (p.startsWith(JDBC_SINK_CONNECTION_FACTORY_PROPERTIES_PREFIX)
                    && !p.equals(STATIC_JDBC_SINK_CONNECTION_FACTORY_URL)) {
                connectionProperties.put(p, v);
            }
        });

        return new JdbcSinkConfiguration.ConnectionFactoryConfiguration(url, connectionProperties);
    }
}
