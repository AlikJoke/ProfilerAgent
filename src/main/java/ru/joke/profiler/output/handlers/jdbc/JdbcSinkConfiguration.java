package ru.joke.profiler.output.handlers.jdbc;

import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.meta.ProfilerDefaultEnumProperty;
import ru.joke.profiler.output.handlers.util.pool.ConnectionPoolConfiguration;

import java.util.Map;
import java.util.Properties;

import static ru.joke.profiler.configuration.ConfigurationProperties.*;

final class JdbcSinkConfiguration {

    private final OutputDataInsertionConfiguration dataInsertionConfiguration;
    private final OutputTableConfiguration outputTableConfiguration;
    private final ConnectionPoolConfiguration connectionPoolConfiguration;
    private final ConnectionFactoryConfiguration connectionFactoryConfiguration;

    @ProfilerConfigurationPropertiesWrapper(prefix = JDBC_SINK_PROPERTIES_PREFIX)
    JdbcSinkConfiguration(
            final OutputDataInsertionConfiguration dataInsertionConfiguration,
            final OutputTableConfiguration outputTableConfiguration,
            final ConnectionPoolConfiguration connectionPoolConfiguration,
            final ConnectionFactoryConfiguration connectionFactoryConfiguration
    ) {
        this.dataInsertionConfiguration = dataInsertionConfiguration;
        this.outputTableConfiguration = outputTableConfiguration;
        this.connectionPoolConfiguration = connectionPoolConfiguration;
        this.connectionFactoryConfiguration = connectionFactoryConfiguration;
    }

    OutputTableConfiguration outputTableConfiguration() {
        return outputTableConfiguration;
    }

    ConnectionPoolConfiguration connectionPoolConfiguration() {
        return connectionPoolConfiguration;
    }

    ConnectionFactoryConfiguration connectionFactoryConfiguration() {
        return connectionFactoryConfiguration;
    }

    OutputDataInsertionConfiguration dataInsertionConfiguration() {
        return dataInsertionConfiguration;
    }

    @Override
    public String toString() {
        return "JdbcSinkConfiguration{"
                + "dataInsertionConfiguration=" + dataInsertionConfiguration
                + ", outputTableConfiguration=" + outputTableConfiguration
                + ", connectionPoolConfiguration=" + connectionPoolConfiguration
                + ", connectionFactoryConfiguration=" + connectionFactoryConfiguration
                + '}';
    }

    static class OutputDataInsertionConfiguration {

        private final boolean enableBatching;
        private final int batchSize;

        @ProfilerConfigurationPropertiesWrapper(prefix = JDBC_SINK_INSERTION_PROPERTIES_PREFIX)
        OutputDataInsertionConfiguration(
                @ProfilerConfigurationProperty(name = STATIC_JDBC_SINK_ENABLE_BATCHING, defaultValue = "true") final boolean enableBatching,
                @ProfilerConfigurationProperty(name = STATIC_JDBC_SINK_BATCH_SIZE, defaultValue = "100") final int batchSize
        ) {
            this.enableBatching = enableBatching;
            this.batchSize = batchSize;
        }

        boolean enableBatching() {
            return enableBatching;
        }

        int batchSize() {
            return batchSize;
        }

        @Override
        public String toString() {
            return "OutputDataInsertionConfiguration{"
                    + "enableBatching=" + enableBatching
                    + ", batchSize=" + batchSize
                    + '}';
        }
    }

    static class OutputTableConfiguration {

        private final String tableName;
        private final ExistingTablePolicy existingTablePolicy;
        private final boolean autoCreateTableIfNotExist;
        private final boolean skipSchemaValidation;
        private final Map<String, ColumnMetadata> columnsMetadata;

        @ProfilerConfigurationPropertiesWrapper(prefix = JDBC_SINK_OUTPUT_TABLE_PROPERTIES_PREFIX)
        OutputTableConfiguration(
                @ProfilerConfigurationProperty(name = STATIC_JDBC_SINK_OUTPUT_TABLE_NAME, required = true) final String tableName,
                @ProfilerConfigurationProperty(name = STATIC_JDBC_SINK_EXISTING_TABLE_POLICY) final ExistingTablePolicy existingTablePolicy,
                @ProfilerConfigurationProperty(name = STATIC_JDBC_SINK_AUTO_CREATE_OUTPUT_TABLE, defaultValue = "true") final boolean autoCreateTableIfNotExist,
                @ProfilerConfigurationProperty(name = STATIC_JDBC_SINK_SKIP_SCHEMA_VALIDATION, defaultValue = "false") final boolean skipSchemaValidation,
                @ProfilerConfigurationProperty(name = STATIC_JDBC_SINK_COLUMNS_METADATA, required = true, parser = JdbcColumnsMetadataPropertyParser.class) final Map<String, ColumnMetadata> columnsMetadata
        ) {
            this.tableName = tableName;
            this.existingTablePolicy = existingTablePolicy;
            this.autoCreateTableIfNotExist = autoCreateTableIfNotExist;
            this.skipSchemaValidation = skipSchemaValidation;
            this.columnsMetadata = columnsMetadata;
        }

        String tableName() {
            return tableName;
        }

        ExistingTablePolicy existingTablePolicy() {
            return existingTablePolicy;
        }

        boolean autoCreateTableIfNotExist() {
            return autoCreateTableIfNotExist;
        }

        boolean skipSchemaValidation() {
            return skipSchemaValidation;
        }

        Map<String, ColumnMetadata> columnsMetadata() {
            return columnsMetadata;
        }

        @Override
        public String toString() {
            return "OutputTableConfiguration{"
                    + "tableName='" + tableName + '\''
                    + ", existingTablePolicy=" + existingTablePolicy
                    + ", autoCreateTableIfNotExist=" + autoCreateTableIfNotExist
                    + ", skipSchemaValidation=" + skipSchemaValidation
                    + ", columnsMetadata=" + columnsMetadata
                    + '}';
        }

        static class ColumnMetadata {

            private final String columnName;
            private final String columnType;

            ColumnMetadata(final String columnName, final String columnType) {
                this.columnName = columnName;
                this.columnType = columnType;
            }

            String columnName() {
                return columnName;
            }

            String columnType() {
                return columnType;
            }

            @Override
            public String toString() {
                return "ColumnMetadata{"
                        + "columnName='" + columnName + '\''
                        + ", columnType='" + columnType + '\''
                        + '}';
            }
        }

        enum ExistingTablePolicy {

            RECREATE,

            @ProfilerDefaultEnumProperty
            NONE,

            TRUNCATE
        }
    }

    static class ConnectionFactoryConfiguration {

        private final String url;
        private final Properties connectionProperties;

        @ProfilerConfigurationPropertiesWrapper(prefix = JDBC_SINK_CONNECTION_FACTORY_PROPERTIES_PREFIX)
        ConnectionFactoryConfiguration(
                @ProfilerConfigurationProperty(name = STATIC_JDBC_SINK_CONNECTION_FACTORY_URL, required = true) final String url,
                @ProfilerConfigurationPropertiesWrapper(parser = JdbcConnectionFactoryPropertiesConfigurationParser.class) final Properties connectionProperties
        ) {
            this.url = url;
            this.connectionProperties = connectionProperties;
        }

        String url() {
            return url;
        }

        Properties connectionProperties() {
            return connectionProperties;
        }

        @Override
        public String toString() {
            return "ConnectionFactoryConfiguration{"
                    + "url='" + url + '\''
                    + ", connectionProperties=" + connectionProperties
                    + '}';
        }
    }
}
