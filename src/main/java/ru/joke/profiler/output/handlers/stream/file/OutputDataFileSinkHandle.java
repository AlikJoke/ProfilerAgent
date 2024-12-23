package ru.joke.profiler.output.handlers.stream.file;

import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.util.NoProfilingOutputDataSinkWrapper;
import ru.joke.profiler.output.handlers.util.OutputDataConversionSinkWrapper;
import ru.joke.profiler.output.handlers.OutputDataSink;
import ru.joke.profiler.output.handlers.stream.OutputDataStreamSinkHandle;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static ru.joke.profiler.configuration.ConfigurationProperties.*;

public final class OutputDataFileSinkHandle extends OutputDataStreamSinkHandle {

    public static final String SINK_TYPE = "file";

    public OutputDataFileSinkHandle() {
        super(STATIC_FILE_SINK_DATA_FORMAT);
    }

    @Override
    public String type() {
        return SINK_TYPE;
    }

    @Override
    protected Function<OutputData, Supplier<String>> conversionFunction(
            final Map<String, String> properties,
            final Map<String, Object> context) {
        return getFormatter(context)::formatLater;
    }

    @Override
    protected OutputDataSink<OutputData> createSyncOutputSink(
            final Map<String, String> properties,
            final Map<String, Object> context) throws IOException {
        final OutputDataSink<String> targetSink = createTerminalOutputSink(properties, context);
        return new OutputDataConversionSinkWrapper<>(targetSink, getFormatter(context)::format);
    }

    @Override
    protected OutputDataSink<String> createTerminalOutputSink(
            final Map<String, String> properties,
            final Map<String, Object> context) throws IOException {
        final String filePath = findRequiredProperty(properties, STATIC_FILE_SINK_FILE);
        final int bufferSize = extractBufferSizeProperty(properties, STATIC_FILE_SINK_BUFFER_SIZE);

        final String existingFilePolicyStr = properties.get(STATIC_FILE_SINK_EXISTING_FILE_POLICY);
        final ExistingFilePolicy existingFilePolicy = ExistingFilePolicy.parse(existingFilePolicyStr);

        final boolean forceFlushOnWrites = parseBooleanProperty(properties, STATIC_FILE_SINK_FORCE_FLUSH_ON_WRITES);
        final OutputDataSink<String> terminalSink = new OutputDataFileSink(
                bufferSize,
                existingFilePolicy,
                filePath,
                forceFlushOnWrites
        );

        return new NoProfilingOutputDataSinkWrapper<>(terminalSink);
    }
}
