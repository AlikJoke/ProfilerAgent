package ru.joke.profiler.output.sinks.fs.stream.file;

import ru.joke.profiler.output.sinks.OutputDataSink;
import ru.joke.profiler.output.sinks.fs.OutputDataAbstractFsSinkHandle;
import ru.joke.profiler.output.sinks.util.NoProfilingOutputDataSinkWrapper;

import java.io.IOException;
import java.util.Map;

public final class OutputDataFileSinkHandle extends OutputDataAbstractFsSinkHandle<FileSinkConfiguration> {

    public static final String SINK_TYPE = "file";

    @Override
    public String type() {
        return SINK_TYPE;
    }

    @Override
    protected OutputDataSink<String> createTerminalOutputSink(
            final FileSinkConfiguration configuration,
            final Map<String, Object> context
    ) throws IOException {

        final OutputDataSink<String> terminalSink = new OutputDataFileSink(configuration);
        return new NoProfilingOutputDataSinkWrapper<>(terminalSink);
    }

    @Override
    protected Class<FileSinkConfiguration> configurationType() {
        return FileSinkConfiguration.class;
    }
}
