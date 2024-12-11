package ru.joke.profiler.core.output;

import ru.joke.profiler.core.configuration.DynamicProfilingConfigurationHolder;
import ru.joke.profiler.core.configuration.StaticProfilingConfiguration;
import ru.joke.profiler.core.output.handlers.OutputDataSink;

public final class ExecutionTimeRegistrarInitializer {

    private final StaticProfilingConfiguration staticConfiguration;
    private final DynamicProfilingConfigurationHolder dynamicProfilingConfigurationHolder;
    private final OutputDataSink outputSink;

    public ExecutionTimeRegistrarInitializer(
            final StaticProfilingConfiguration staticConfiguration,
            final DynamicProfilingConfigurationHolder dynamicProfilingConfigurationHolder,
            final OutputDataSink outputSink) {
        this.staticConfiguration = staticConfiguration;
        this.outputSink = outputSink;
        this.dynamicProfilingConfigurationHolder = dynamicProfilingConfigurationHolder;
    }

    public void init() {
        final ExecutionTimeRegistrar baseRegistrar =
                this.staticConfiguration.isExecutionTracingEnabled()
                        ? new TracedExecutionTimeRegistrar(this.outputSink)
                        : new SimpleExecutionTimeRegistrar(this.outputSink);
        final ExecutionTimeRegistrar resultRegistrar =
                this.staticConfiguration.isDynamicConfigurationEnabled()
                        ? new DynamicConfigurableExecutionTimeRegistrar(baseRegistrar, this.staticConfiguration, this.dynamicProfilingConfigurationHolder)
                        : baseRegistrar;
        resultRegistrar.init();
    }
}
