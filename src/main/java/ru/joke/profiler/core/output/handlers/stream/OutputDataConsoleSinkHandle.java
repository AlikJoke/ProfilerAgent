package ru.joke.profiler.core.output.handlers.stream;

import ru.joke.profiler.core.output.handlers.OutputDataSink;

import java.util.Map;

import static ru.joke.profiler.core.configuration.ConfigurationProperties.*;

public final class OutputDataConsoleSinkHandle extends OutputDataStreamSinkHandle {

    public static final String SINK_TYPE = "console";

    @Override
    public String type() {
        return SINK_TYPE;
    }

    @Override
    public OutputDataSink create(final Map<String, String> properties) throws Exception {
        return new OutputDataConsoleSink(
                createFormatter(properties, STATIC_CONSOLE_SINK_DATA_FORMAT),
                extractBufferSizeProperty(properties, STATIC_CONSOLE_SINK_BUFFER_SIZE),
                extractFlushIntervalProperty(properties, STATIC_CONSOLE_SINK_FLUSH_INTERVAL)
        );
    }

}
