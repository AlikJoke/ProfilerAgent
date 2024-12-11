package ru.joke.profiler.core.output.handlers.stream;

import ru.joke.profiler.core.configuration.InvalidConfigurationException;
import ru.joke.profiler.core.output.handlers.OutputDataSink;

import java.util.Map;

import static ru.joke.profiler.core.configuration.ConfigurationProperties.*;

public final class OutputDataFileSinkHandle extends OutputDataStreamSinkHandle {

    public static final String SINK_TYPE = "file";

    @Override
    public String type() {
        return SINK_TYPE;
    }

    @Override
    public OutputDataSink create(final Map<String, String> properties) throws Exception {
        final String filePath = properties.get(STATIC_FILE_SINK_FILE);
        if (filePath == null || filePath.isEmpty()) {
            throw new InvalidConfigurationException(String.format("File path property (%s) is required for file sink", STATIC_FILE_SINK_FILE));
        }
        return new OutputDataFileSink(
                createFormatter(properties, STATIC_FILE_SINK_DATA_FORMAT),
                extractBufferSizeProperty(properties, STATIC_FILE_SINK_BUFFER_SIZE),
                extractFlushIntervalProperty(properties, STATIC_FILE_SINK_FLUSH_INTERVAL),
                filePath
        );
    }

}
