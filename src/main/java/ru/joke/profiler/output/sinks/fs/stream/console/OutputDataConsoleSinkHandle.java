package ru.joke.profiler.output.sinks.fs.stream.console;

import ru.joke.profiler.output.sinks.OutputDataSink;
import ru.joke.profiler.output.sinks.fs.OutputDataAbstractFsSinkHandle;
import ru.joke.profiler.output.sinks.util.NoProfilingOutputDataSinkWrapper;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public final class OutputDataConsoleSinkHandle extends OutputDataAbstractFsSinkHandle<ConsoleSinkConfiguration> {

    public static final String SINK_TYPE = "console";

    @Override
    public String type() {
        return SINK_TYPE;
    }

    @Override
    protected Class<ConsoleSinkConfiguration> configurationType() {
        return ConsoleSinkConfiguration.class;
    }

    @Override
    protected OutputDataSink<String> createTerminalOutputSink(
            final ConsoleSinkConfiguration configuration,
            final Map<String, Object> context
    ) throws UnsupportedEncodingException {
        final OutputDataSink<String> terminalSink = new OutputDataConsoleSink(configuration);
        return new NoProfilingOutputDataSinkWrapper<>(terminalSink);
    }
}
