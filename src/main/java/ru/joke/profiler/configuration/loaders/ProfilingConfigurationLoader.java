package ru.joke.profiler.configuration.loaders;

import ru.joke.profiler.configuration.DynamicProfilingConfiguration;
import ru.joke.profiler.configuration.StaticProfilingConfiguration;
import ru.joke.profiler.configuration.meta.ConfigurationParser;

import java.util.Map;

public final class ProfilingConfigurationLoader {

    private final ConfigurationPropertiesLoader propertiesLoader;

    ProfilingConfigurationLoader(final ConfigurationPropertiesLoader propertiesLoader) {
        this.propertiesLoader = propertiesLoader;
    }

    public DynamicProfilingConfiguration loadDynamic() {
        final Map<String, String> properties = this.propertiesLoader.load();
        if (properties.isEmpty()) {
            return null;
        }

        return ConfigurationParser.parse(DynamicProfilingConfiguration.class, properties);
    }

    public StaticProfilingConfiguration loadStatic() {
        final Map<String, String> properties = this.propertiesLoader.load();
        return ConfigurationParser.parse(StaticProfilingConfiguration.class, properties);
    }
}
