package ru.joke.profiler.output.sinks.fs;

import ru.joke.profiler.output.sinks.async.AsyncOutputDataSinkConfigurationSupport;
import ru.joke.profiler.output.sinks.async.AsyncSinkDataFlushingConfiguration;

public abstract class AbstractFsSinkConfiguration extends AsyncOutputDataSinkConfigurationSupport {

    protected static final String OUTPUT_DATA_PATTERN = "output_data_pattern";

    protected final String outputDataPattern;

    protected AbstractFsSinkConfiguration(
            final String outputDataPattern,
            final AsyncSinkDataFlushingConfiguration asyncFlushingConfiguration
    ) {
        super(asyncFlushingConfiguration);
        this.outputDataPattern = outputDataPattern;
    }

    public String outputDataPattern() {
        return outputDataPattern;
    }
}
