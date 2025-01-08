package ru.joke.profiler.configuration.loaders;

import java.util.Map;

public interface ConfigurationPropertiesLoader {

    Map<String, String> load();
}
