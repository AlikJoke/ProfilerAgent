package ru.joke.profiler.output.handlers.fs.jul;

import ru.joke.profiler.output.handlers.OutputDataSink;
import ru.joke.profiler.output.handlers.fs.OutputDataAbstractFsSinkHandle;
import ru.joke.profiler.output.handlers.util.NoProfilingOutputDataSinkWrapper;

import java.util.Map;

public final class OutputDataLoggerSinkHandle extends OutputDataAbstractFsSinkHandle<LoggerSinkConfiguration> {

    public static final String SINK_TYPE = "logger";

    @Override
    public String type() {
        return SINK_TYPE;
    }

    @Override
    protected OutputDataSink<String> createTerminalOutputSink(
            final Map<String, String> properties,
            final Map<String, Object> context
    ) {
        final LoggerSinkConfiguration sinkConfiguration = getConfiguration(context);
        final OutputDataSink<String> terminalSink = new OutputDataLoggerSink(sinkConfiguration);

        return new NoProfilingOutputDataSinkWrapper<>(terminalSink);
    }

    @Override
    protected Class<LoggerSinkConfiguration> configurationType() {
        return LoggerSinkConfiguration.class;
    }
}
