package ru.joke.profiler.configuration.loaders;

import ru.joke.profiler.configuration.meta.ConfigurationPropertyParser;
import ru.joke.profiler.configuration.meta.DefaultConfigurationPropertyParser;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.meta.StatelessParser;

@StatelessParser
final class VaultConfigurationPropertyParser extends DefaultConfigurationPropertyParser implements ConfigurationPropertyParser<Object> {

    private static final String START_PROPERTY_MARKER = "${";
    private static final String END_PROPERTY_MARKER = "}";

    @Override
    public Object parse(
            final ProfilerConfigurationProperty property,
            final Class<Object> propertyType,
            final String propertyValue
    ) {
        final String newPropertyValue = findVaultPropertyValue(propertyValue);
        return super.parse(property, propertyType, newPropertyValue);
    }

    private String findVaultPropertyValue(final String propertyValue) {
        final int endIndex = propertyValue.indexOf(END_PROPERTY_MARKER);
        if (!propertyValue.startsWith(START_PROPERTY_MARKER) || endIndex < 0) {
            return propertyValue;
        }

        final String propertyName = propertyValue.substring(START_PROPERTY_MARKER.length(), endIndex);
        final String[] propertyInfoParts = propertyName.split(":");
        final String envPropertyValue = System.getenv(propertyInfoParts[0]);
        return envPropertyValue == null
                ? propertyInfoParts.length == 2
                    ? propertyInfoParts[1]
                    : ""
                : envPropertyValue;
    }
}
