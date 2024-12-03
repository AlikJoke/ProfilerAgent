package ru.joke.profiler.core.output;

import ru.joke.profiler.core.configuration.StaticProfilingConfiguration;

public final class ExecutionTimeRegistrarInitializer {

    private final StaticProfilingConfiguration staticConfiguration;

    public ExecutionTimeRegistrarInitializer(final StaticProfilingConfiguration staticConfiguration) {
        this.staticConfiguration = staticConfiguration;
    }

    public void init() {
        final ExecutionTimeRegistrar registrar =
                this.staticConfiguration.isExecutionTracingEnabled()
                        ? new TracedExecutionTimeRegistrar()
                        : new SimpleExecutionTimeRegistrar();
        registrar.init();
    }
}
