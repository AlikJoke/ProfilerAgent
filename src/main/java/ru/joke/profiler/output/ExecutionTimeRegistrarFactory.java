package ru.joke.profiler.output;

import ru.joke.profiler.configuration.DynamicProfilingConfigurationHolder;
import ru.joke.profiler.configuration.StaticProfilingConfiguration;
import ru.joke.profiler.output.sinks.OutputData;
import ru.joke.profiler.output.sinks.OutputDataSink;

public final class ExecutionTimeRegistrarFactory {

    private final StaticProfilingConfiguration staticConfiguration;
    private final DynamicProfilingConfigurationHolder dynamicProfilingConfigurationHolder;
    private final OutputDataSink<OutputData> outputSink;

    public ExecutionTimeRegistrarFactory(
            final StaticProfilingConfiguration staticConfiguration,
            final DynamicProfilingConfigurationHolder dynamicProfilingConfigurationHolder,
            final OutputDataSink<OutputData> outputSink
    ) {
        this.staticConfiguration = staticConfiguration;
        this.outputSink = outputSink;
        this.dynamicProfilingConfigurationHolder = dynamicProfilingConfigurationHolder;
    }

    public ExecutionTimeRegistrar create() {
        final ExecutionTimeRegistrar baseRegistrar =
                this.staticConfiguration.executionTracingEnabled()
                        ? new TracedExecutionTimeRegistrar(this.outputSink)
                        : new SimpleExecutionTimeRegistrar(this.outputSink);
        final ExecutionTimeRegistrar resultRegistrar =
                this.staticConfiguration.dynamicConfigurationEnabled()
                        ? new DynamicConfigurableExecutionTimeRegistrar(baseRegistrar, this.staticConfiguration, this.dynamicProfilingConfigurationHolder)
                        : baseRegistrar;
        resultRegistrar.init();

        return resultRegistrar;
    }
}
