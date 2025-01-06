package ru.joke.profiler.output.sinks.fs.jul;

import ru.joke.profiler.output.sinks.OutputDataSink;
import ru.joke.profiler.output.sinks.fs.OutputDataAbstractFsSinkHandle;
import ru.joke.profiler.output.sinks.util.NoProfilingOutputDataSinkWrapper;

import java.util.Map;

public final class OutputDataLoggerSinkHandle extends OutputDataAbstractFsSinkHandle<LoggerSinkConfiguration> {

    public static final String SINK_TYPE = "logger";

    @Override
    public String type() {
        return SINK_TYPE;
    }

    @Override
    protected OutputDataSink<String> createTerminalOutputSink(
            final LoggerSinkConfiguration configuration,
            final Map<String, Object> context
    ) {
        final OutputDataSink<String> terminalSink = new OutputDataLoggerSink(configuration);
        return new NoProfilingOutputDataSinkWrapper<>(terminalSink);
    }

    @Override
    protected Class<LoggerSinkConfiguration> configurationType() {
        return LoggerSinkConfiguration.class;
    }
}
