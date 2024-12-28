package ru.joke.profiler.configuration.util;

import ru.joke.profiler.configuration.meta.ConfigurationParser;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.StatelessParser;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;

@StatelessParser
public final class MapConfigurationPropertiesParser implements ConfigurationParser<Map<String, String>> {

    @Override
    public Map<String, String> parse(
            final Class<Map<String, String>> type,
            final AnnotatedElement annotatedElement,
            final ProfilerConfigurationPropertiesWrapper configuration,
            final Map<String, String> properties
    ) {
        return properties;
    }
}
