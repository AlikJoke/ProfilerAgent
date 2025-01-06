package ru.joke.profiler.output.sinks.jdbc;

import ru.joke.profiler.configuration.InvalidConfigurationException;
import ru.joke.profiler.configuration.meta.ConfigurationPropertyParser;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.meta.StatelessParser;

import java.util.LinkedHashMap;
import java.util.Map;

@StatelessParser
final class JdbcColumnsMetadataPropertyParser implements ConfigurationPropertyParser<Map<String, JdbcSinkConfiguration.OutputTableConfiguration.ColumnMetadata>> {

    @Override
    public Map<String, JdbcSinkConfiguration.OutputTableConfiguration.ColumnMetadata> parse(
            final ProfilerConfigurationProperty property,
            final Class<Map<String, JdbcSinkConfiguration.OutputTableConfiguration.ColumnMetadata>> propertyType,
            final String propertyValue
    ) {
        final String[] columnsMetadataParts = propertyValue.split(";");
        final Map<String, JdbcSinkConfiguration.OutputTableConfiguration.ColumnMetadata> result = new LinkedHashMap<>();
        for (final String columnMetadata : columnsMetadataParts) {
            if (columnMetadata == null || columnMetadata.isEmpty()) {
                throw new InvalidConfigurationException("Column metadata shouldn't be empty");
            }

            final String[] columnParts = columnMetadata.split(":");
            if (columnParts.length > 3 || columnParts.length < 2) {
                throw new InvalidConfigurationException("Column metadata must contain 2 or 3 parts separated by the colon: " + columnMetadata);
            }

            final String propertyName = columnParts[0];
            final String columnNamePart = columnParts[columnParts.length - 2];
            final String column = columnNamePart.isEmpty() ? propertyName : columnNamePart;
            final String type = columnParts[columnParts.length - 1];

            if (propertyName.isEmpty() || type.isEmpty()) {
                throw new InvalidConfigurationException("Property or column type is not set: " + columnMetadata);
            }

            result.put(propertyName, new JdbcSinkConfiguration.OutputTableConfiguration.ColumnMetadata(column, type));
        }

        return result;
    }
}
