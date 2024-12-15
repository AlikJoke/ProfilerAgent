package ru.joke.profiler.core.output.handlers.jdbc;

import java.util.Map;
import java.util.Properties;

final class JdbcSinkConfiguration {

    private final OutputDataInsertionConfiguration dataInsertionConfiguration;
    private final OutputTableConfiguration outputTableConfiguration;
    private final ConnectionPoolConfiguration connectionPoolConfiguration;
    private final ConnectionFactoryConfiguration connectionFactoryConfiguration;

    JdbcSinkConfiguration(
            final OutputDataInsertionConfiguration dataInsertionConfiguration,
            final OutputTableConfiguration outputTableConfiguration,
            final ConnectionPoolConfiguration connectionPoolConfiguration,
            final ConnectionFactoryConfiguration connectionFactoryConfiguration) {
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

        OutputDataInsertionConfiguration(final boolean enableBatching, final int batchSize) {
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

        OutputTableConfiguration(
                final String tableName,
                final ExistingTablePolicy existingTablePolicy,
                final boolean autoCreateTableIfNotExist,
                boolean skipSchemaValidation,
                final Map<String, ColumnMetadata> columnsMetadata) {
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
    }

    static class ConnectionFactoryConfiguration {

        private final String url;
        private final Properties connectionProperties;

        ConnectionFactoryConfiguration(final String url, final Properties connectionProperties) {
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

    static class ConnectionPoolConfiguration {

        private final int maxPoolSize;
        private final int initialPoolSize;
        private final long keepAliveIdleTime;
        private final long maxConnectionWaitTime;

        ConnectionPoolConfiguration(
                final int maxPoolSize,
                final int initialPoolSize,
                final long keepAliveIdleTime,
                final long maxConnectionWaitTime) {
            this.maxPoolSize = maxPoolSize;
            this.initialPoolSize = initialPoolSize;
            this.keepAliveIdleTime = keepAliveIdleTime;
            this.maxConnectionWaitTime = maxConnectionWaitTime;
        }

        int maxPoolSize() {
            return maxPoolSize;
        }

        int initialPoolSize() {
            return initialPoolSize;
        }

        long keepAliveIdleTime() {
            return keepAliveIdleTime;
        }

        long maxConnectionWaitTime() {
            return maxConnectionWaitTime;
        }

        @Override
        public String toString() {
            return "ConnectionPoolConfiguration{"
                    + "maxPoolSize=" + maxPoolSize
                    + ", initialPoolSize=" + initialPoolSize
                    + ", keepAliveIdleTime=" + keepAliveIdleTime
                    + ", maxConnectionWaitTime=" + maxConnectionWaitTime
                    + '}';
        }
    }
}
