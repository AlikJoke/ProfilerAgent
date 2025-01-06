package ru.joke.profiler.output.sinks.fs.stream.console;

import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.output.sinks.async.AsyncSinkDataFlushingConfiguration;
import ru.joke.profiler.output.sinks.fs.stream.AbstractFsStreamSinkConfiguration;

import static ru.joke.profiler.output.sinks.fs.stream.console.OutputDataConsoleSinkHandle.SINK_TYPE;

public final class ConsoleSinkConfiguration extends AbstractFsStreamSinkConfiguration {

    private static final String CONSOLE_SINK_PROPERTIES_PREFIX = SINK_TYPE + ".";

    @ProfilerConfigurationPropertiesWrapper(prefix = CONSOLE_SINK_PROPERTIES_PREFIX)
    public ConsoleSinkConfiguration(
            @ProfilerConfigurationProperty(name = OUTPUT_DATA_PATTERN) final String outputDataPattern,
            @ProfilerConfigurationProperty(name = OUTPUT_STREAM_BUFFER_SIZE, defaultValue = "8192") final int bufferSize,
            @ProfilerConfigurationProperty(name = OUTPUT_STREAM_FORCE_FLUSH_ON_WRITES) final boolean forceFlushOnWrites,
            final AsyncSinkDataFlushingConfiguration asyncFlushingConfiguration
    ) {
        super(outputDataPattern, bufferSize, forceFlushOnWrites, asyncFlushingConfiguration);
    }

    @Override
    public String toString() {
        return "ConsoleSinkConfiguration{"
                + "bufferSize=" + bufferSize
                + ", forceFlushOnWrites=" + forceFlushOnWrites
                + ", outputDataPattern='" + outputDataPattern + '\''
                + ", asyncFlushingConfiguration=" + asyncFlushingConfiguration
                + '}';
    }
}
