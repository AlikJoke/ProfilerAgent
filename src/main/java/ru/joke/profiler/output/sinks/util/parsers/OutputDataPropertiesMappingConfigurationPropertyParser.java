package ru.joke.profiler.output.sinks.util.parsers;

import ru.joke.profiler.configuration.InvalidConfigurationException;
import ru.joke.profiler.configuration.meta.ConfigurationPropertyParser;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.meta.StatelessParser;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@StatelessParser
public final class OutputDataPropertiesMappingConfigurationPropertyParser implements ConfigurationPropertyParser<Map<String, String>> {

    @Override
    public Map<String, String> parse(
            final ProfilerConfigurationProperty property,
            final Class<Map<String, String>> propertyType,
            final String propertyValue) {

        if (propertyValue == null || propertyValue.isEmpty()) {

            if (property.required()) {
                throw new InvalidConfigurationException(String.format("Property %s isn't provided", property.name()));
            }

            return Collections.emptyMap();
        }

        return Arrays.stream(propertyValue.split(";"))
                .map(mappingParts -> mappingParts.split(":"))
                .collect(Collectors.toMap(mapping -> mapping[0], mapping -> mapping[mapping.length - 1]));
    }
}
