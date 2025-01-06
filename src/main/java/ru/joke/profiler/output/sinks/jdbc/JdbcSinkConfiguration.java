package ru.joke.profiler.output.sinks.jdbc;

import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.meta.ProfilerDefaultEnumProperty;
import ru.joke.profiler.configuration.util.MapConfigurationPropertiesParser;
import ru.joke.profiler.output.sinks.async.AsyncOutputDataSinkConfigurationSupport;
import ru.joke.profiler.output.sinks.async.AsyncSinkDataFlushingConfiguration;
import ru.joke.profiler.output.sinks.util.pool.ConnectionPoolConfiguration;

import java.util.Map;
import java.util.Properties;

import static ru.joke.profiler.output.sinks.jdbc.OutputDataJdbcSinkHandle.SINK_TYPE;

public final class JdbcSinkConfiguration extends AsyncOutputDataSinkConfigurationSupport {

    private static final String JDBC_SINK_PROPERTIES_PREFIX = SINK_TYPE + ".";

    private final OutputDataInsertionConfiguration dataInsertionConfiguration;
    private final OutputTableConfiguration outputTableConfiguration;
    private final ConnectionPoolConfiguration connectionPoolConfiguration;
    private final ConnectionFactoryConfiguration connectionFactoryConfiguration;

    @ProfilerConfigurationPropertiesWrapper(prefix = JDBC_SINK_PROPERTIES_PREFIX)
    public JdbcSinkConfiguration(
            final OutputDataInsertionConfiguration dataInsertionConfiguration,
            final OutputTableConfiguration outputTableConfiguration,
            final ConnectionPoolConfiguration connectionPoolConfiguration,
            final ConnectionFactoryConfiguration connectionFactoryConfiguration,
            final AsyncSinkDataFlushingConfiguration asyncFlushingConfiguration
    ) {
        super(asyncFlushingConfiguration);
        this.dataInsertionConfiguration = dataInsertionConfiguration;
        this.outputTableConfiguration = outputTableConfiguration;
        this.connectionPoolConfiguration = connectionPoolConfiguration;
        this.connectionFactoryConfiguration = connectionFactoryConfiguration;
    }

    public OutputTableConfiguration outputTableConfiguration() {
        return outputTableConfiguration;
    }

    public ConnectionPoolConfiguration connectionPoolConfiguration() {
        return connectionPoolConfiguration;
    }

    public ConnectionFactoryConfiguration connectionFactoryConfiguration() {
        return connectionFactoryConfiguration;
    }

    public OutputDataInsertionConfiguration dataInsertionConfiguration() {
        return dataInsertionConfiguration;
    }

    @Override
    public String toString() {
        return "JdbcSinkConfiguration{"
                + "dataInsertionConfiguration=" + dataInsertionConfiguration
                + ", outputTableConfiguration=" + outputTableConfiguration
                + ", connectionPoolConfiguration=" + connectionPoolConfiguration
                + ", connectionFactoryConfiguration=" + connectionFactoryConfiguration
                + ", asyncFlushingConfiguration=" + asyncFlushingConfiguration
                + '}';
    }

    public static class OutputDataInsertionConfiguration {

        private static final String INSERTION_PROPERTIES_PREFIX = "insertion.";

        private static final String DISABLE_BATCHING = "disable_batching";
        private static final String BATCH_SIZE = "batch_size";

        private final boolean enableBatching;
        private final int batchSize;

        @ProfilerConfigurationPropertiesWrapper(prefix = INSERTION_PROPERTIES_PREFIX)
        public OutputDataInsertionConfiguration(
                @ProfilerConfigurationProperty(name = DISABLE_BATCHING) final boolean disableBatching,
                @ProfilerConfigurationProperty(name = BATCH_SIZE, defaultValue = "100") final int batchSize
        ) {
            this.enableBatching = !disableBatching;
            this.batchSize = batchSize;
        }

        public boolean enableBatching() {
            return enableBatching;
        }

        public int batchSize() {
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

    public static class OutputTableConfiguration {

        private static final String OUTPUT_TABLE_PROPERTIES_PREFIX = "output-table.";

        private static final String OUTPUT_TABLE_NAME = "table_name";
        private static final String EXISTING_TABLE_POLICY = "existing_table_policy";
        private static final String AUTO_CREATE_OUTPUT_TABLE = "auto_create_table";
        private static final String SKIP_SCHEMA_VALIDATION = "skip_schema_validation";
        private static final String COLUMNS_METADATA = "columns_metadata";

        private final String tableName;
        private final ExistingTablePolicy existingTablePolicy;
        private final boolean autoCreateTableIfNotExist;
        private final boolean skipSchemaValidation;
        private final Map<String, ColumnMetadata> columnsMetadata;

        @ProfilerConfigurationPropertiesWrapper(prefix = OUTPUT_TABLE_PROPERTIES_PREFIX)
        public OutputTableConfiguration(
                @ProfilerConfigurationProperty(name = OUTPUT_TABLE_NAME, required = true) final String tableName,
                @ProfilerConfigurationProperty(name = EXISTING_TABLE_POLICY) final ExistingTablePolicy existingTablePolicy,
                @ProfilerConfigurationProperty(name = AUTO_CREATE_OUTPUT_TABLE, defaultValue = "true") final boolean autoCreateTableIfNotExist,
                @ProfilerConfigurationProperty(name = SKIP_SCHEMA_VALIDATION) final boolean skipSchemaValidation,
                @ProfilerConfigurationProperty(name = COLUMNS_METADATA, required = true, parser = JdbcColumnsMetadataPropertyParser.class) final Map<String, ColumnMetadata> columnsMetadata
        ) {
            this.tableName = tableName;
            this.existingTablePolicy = existingTablePolicy;
            this.autoCreateTableIfNotExist = autoCreateTableIfNotExist;
            this.skipSchemaValidation = skipSchemaValidation;
            this.columnsMetadata = columnsMetadata;
        }

        public String tableName() {
            return tableName;
        }

        public ExistingTablePolicy existingTablePolicy() {
            return existingTablePolicy;
        }

        public boolean autoCreateTableIfNotExist() {
            return autoCreateTableIfNotExist;
        }

        public boolean skipSchemaValidation() {
            return skipSchemaValidation;
        }

        public Map<String, ColumnMetadata> columnsMetadata() {
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

        public static class ColumnMetadata {

            private final String columnName;
            private final String columnType;

            public ColumnMetadata(final String columnName, final String columnType) {
                this.columnName = columnName;
                this.columnType = columnType;
            }

            public String columnName() {
                return columnName;
            }

            public String columnType() {
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

        public enum ExistingTablePolicy {

            RECREATE,

            @ProfilerDefaultEnumProperty
            NONE,

            TRUNCATE
        }
    }

    public static class ConnectionFactoryConfiguration {

        private static final String CONNECTION_FACTORY_PROPERTIES_PREFIX = "connection-factory.";
        static final String CONNECTION_FACTORY_URL = "url";

        private final String url;
        private final Properties connectionProperties;

        @ProfilerConfigurationPropertiesWrapper(prefix = CONNECTION_FACTORY_PROPERTIES_PREFIX)
        public ConnectionFactoryConfiguration(
                @ProfilerConfigurationProperty(name = CONNECTION_FACTORY_URL, required = true) final String url,
                @ProfilerConfigurationPropertiesWrapper(parser = MapConfigurationPropertiesParser.class) final Map<String, String> connectionProperties
        ) {
            this.url = url;
            this.connectionProperties = new Properties();
            this.connectionProperties.putAll(connectionProperties);
        }

        public String url() {
            return url;
        }

        public Properties connectionProperties() {
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
