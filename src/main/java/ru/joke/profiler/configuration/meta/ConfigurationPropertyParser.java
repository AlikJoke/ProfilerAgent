package ru.joke.profiler.configuration.meta;

public interface ConfigurationPropertyParser<T> {

    T parse(
            ProfilerConfigurationProperty property,
            Class<T> propertyType,
            String propertyValue
    );
}
