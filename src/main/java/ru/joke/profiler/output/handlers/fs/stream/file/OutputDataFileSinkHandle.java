package ru.joke.profiler.output.handlers.fs.stream.file;

import ru.joke.profiler.output.handlers.OutputDataSink;
import ru.joke.profiler.output.handlers.fs.OutputDataAbstractFsSinkHandle;
import ru.joke.profiler.output.handlers.util.NoProfilingOutputDataSinkWrapper;

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
            final Map<String, String> properties,
            final Map<String, Object> context
    ) throws IOException {

        final FileSinkConfiguration configuration = getConfiguration(context);
        final OutputDataSink<String> terminalSink = new OutputDataFileSink(configuration);

        return new NoProfilingOutputDataSinkWrapper<>(terminalSink);
    }

    @Override
    protected Class<FileSinkConfiguration> configurationType() {
        return FileSinkConfiguration.class;
    }
}
