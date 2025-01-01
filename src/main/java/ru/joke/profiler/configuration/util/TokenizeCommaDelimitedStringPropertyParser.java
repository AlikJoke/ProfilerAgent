package ru.joke.profiler.configuration.util;

import ru.joke.profiler.configuration.meta.ConfigurationPropertyParser;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class TokenizeCommaDelimitedStringPropertyParser implements ConfigurationPropertyParser<List<String>> {

    @Override
    public List<String> parse(
            final ProfilerConfigurationProperty property,
            final Class<List<String>> propertyType,
            final String propertyValue
    ) {
        return propertyValue.isEmpty()
                ? Collections.emptyList()
                : Arrays.stream(propertyValue.split(","))
                        .filter(v -> !v.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
    }
}
