package ru.joke.profiler.core.output;

import ru.joke.profiler.core.configuration.DynamicProfilingConfigurationHolder;
import ru.joke.profiler.core.configuration.StaticProfilingConfiguration;

public final class ExecutionTimeRegistrarInitializer {

    private final StaticProfilingConfiguration staticConfiguration;
    private final DynamicProfilingConfigurationHolder dynamicProfilingConfigurationHolder;

    public ExecutionTimeRegistrarInitializer(
            final StaticProfilingConfiguration staticConfiguration,
            final DynamicProfilingConfigurationHolder dynamicProfilingConfigurationHolder) {
        this.staticConfiguration = staticConfiguration;
        this.dynamicProfilingConfigurationHolder = dynamicProfilingConfigurationHolder;
    }

    public void init() {
        final ExecutionTimeRegistrar baseRegistrar =
                this.staticConfiguration.isExecutionTracingEnabled()
                        ? new TracedExecutionTimeRegistrar()
                        : new SimpleExecutionTimeRegistrar();
        final ExecutionTimeRegistrar resultRegistrar =
                this.staticConfiguration.isDynamicConfigurationEnabled()
                        ? new DynamicConfigurableExecutionTimeRegistrar(baseRegistrar, this.staticConfiguration, this.dynamicProfilingConfigurationHolder)
                        : baseRegistrar;
        resultRegistrar.init();
    }
}
