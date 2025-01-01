package ru.joke.profiler.transformation.spy;

import ru.joke.profiler.configuration.DynamicProfilingConfigurationHolder;
import ru.joke.profiler.configuration.StaticProfilingConfiguration;
import ru.joke.profiler.output.ExecutionTimeRegistrar;

public interface SpyContext {

    ExecutionTimeRegistrar registrar();

    StaticProfilingConfiguration staticConfiguration();

    DynamicProfilingConfigurationHolder dynamicConfigurationHolder();
}
