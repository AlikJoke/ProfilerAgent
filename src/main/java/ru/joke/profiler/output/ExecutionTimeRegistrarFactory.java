package ru.joke.profiler.output;

import ru.joke.profiler.configuration.DynamicProfilingConfigurationHolder;
import ru.joke.profiler.configuration.StaticProfilingConfiguration;
import ru.joke.profiler.output.sinks.OutputData;
import ru.joke.profiler.output.sinks.OutputDataSink;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

public final class ExecutionTimeRegistrarFactory {

    private final StaticProfilingConfiguration staticConfiguration;
    private final DynamicProfilingConfigurationHolder dynamicProfilingConfigurationHolder;
    private final OutputDataSink<OutputData> outputSink;

    public ExecutionTimeRegistrarFactory(
            final StaticProfilingConfiguration staticConfiguration,
            final DynamicProfilingConfigurationHolder dynamicProfilingConfigurationHolder,
            final OutputDataSink<OutputData> outputSink
    ) {
        this.staticConfiguration = checkNotNull(staticConfiguration, "staticConfiguration");
        this.outputSink = checkNotNull(outputSink, "outputSink");
        this.dynamicProfilingConfigurationHolder = checkNotNull(dynamicProfilingConfigurationHolder, "dynamicProfilingConfigurationHolder");
    }

    public ExecutionTimeRegistrar create() {
        final ExecutionTimeRegistrar baseRegistrar = new TracedExecutionTimeRegistrar(this.outputSink);
        final ExecutionTimeRegistrar resultRegistrar =
                this.staticConfiguration.dynamicConfigurationEnabled()
                        ? new DynamicConfigurableExecutionTimeRegistrar(baseRegistrar, this.dynamicProfilingConfigurationHolder)
                        : baseRegistrar;
        resultRegistrar.init();

        return resultRegistrar;
    }
}
