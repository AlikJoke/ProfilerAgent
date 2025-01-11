package ru.joke.profiler.configuration.loaders;

import ru.joke.profiler.configuration.DynamicProfilingConfiguration;
import ru.joke.profiler.configuration.StaticProfilingConfiguration;
import ru.joke.profiler.configuration.meta.ConfigurationParser;

import java.util.Map;
import java.util.logging.Logger;

public final class ProfilingConfigurationLoader {

    private static final Logger logger = Logger.getLogger(ProfilingConfigurationLoader.class.getCanonicalName());

    private final ConfigurationPropertiesLoader propertiesLoader;

    ProfilingConfigurationLoader(final ConfigurationPropertiesLoader propertiesLoader) {
        this.propertiesLoader = propertiesLoader;
    }

    public DynamicProfilingConfiguration loadDynamic() {
        final Map<String, String> properties = this.propertiesLoader.load();
        if (properties.isEmpty()) {
            return null;
        }

        final DynamicProfilingConfiguration result = ConfigurationParser.parse(DynamicProfilingConfiguration.class, properties);
        logger.fine("Dynamic configuration loaded: " + result);

        return result;
    }

    public StaticProfilingConfiguration loadStatic() {
        final Map<String, String> properties = this.propertiesLoader.load();
        logger.fine("Loaded properties from source: " + properties);
        final StaticProfilingConfiguration result = ConfigurationParser.parse(StaticProfilingConfiguration.class, properties);
        logger.fine("Static configuration loaded: " + result);

        return result;
    }
}
