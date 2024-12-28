package ru.joke.profiler.output.handlers.stream.console;

import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.util.NoProfilingOutputDataSinkWrapper;
import ru.joke.profiler.output.handlers.util.OutputDataConversionSinkWrapper;
import ru.joke.profiler.output.handlers.OutputDataSink;
import ru.joke.profiler.output.handlers.stream.OutputDataStreamSinkHandle;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static ru.joke.profiler.configuration.ConfigurationProperties.*;

public final class OutputDataConsoleSinkHandle extends OutputDataStreamSinkHandle {

    public static final String SINK_TYPE = "console";

    public OutputDataConsoleSinkHandle() {
        super(STATIC_CONSOLE_SINK_DATA_FORMAT);
    }

    @Override
    public String type() {
        return SINK_TYPE;
    }

    @Override
    protected Function<OutputData, Supplier<String>> conversionFunction(
            final Map<String, String> properties,
            final Map<String, Object> context
    ) {
        return getFormatter(context)::formatLater;
    }

    @Override
    protected OutputDataSink<OutputData> createSyncOutputSink(
            final Map<String, String> properties,
            final Map<String, Object> context
    ) throws UnsupportedEncodingException {
        final OutputDataSink<String> targetSink = createTerminalOutputSink(properties, context);
        return new OutputDataConversionSinkWrapper<>(targetSink, getFormatter(context)::format);
    }

    @Override
    protected OutputDataSink<String> createTerminalOutputSink(
            final Map<String, String> properties,
            final Map<String, Object> context
    ) throws UnsupportedEncodingException {
        final boolean forceFlushOnWrites = parseBooleanProperty(properties, STATIC_CONSOLE_SINK_FORCE_FLUSH_ON_WRITES);
        final int bufferSize = extractBufferSizeProperty(properties, STATIC_CONSOLE_SINK_BUFFER_SIZE);

        final OutputDataSink<String> terminalSink = new OutputDataConsoleSink(
                bufferSize,
                forceFlushOnWrites
        );

        return new NoProfilingOutputDataSinkWrapper<>(terminalSink);
    }
}
