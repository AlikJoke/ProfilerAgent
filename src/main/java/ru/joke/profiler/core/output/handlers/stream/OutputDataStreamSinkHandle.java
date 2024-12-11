package ru.joke.profiler.core.output.handlers.stream;

import ru.joke.profiler.core.output.handlers.OutputDataSinkHandle;
import ru.joke.profiler.core.output.handlers.OutputStringDataFormatter;
import ru.joke.profiler.core.output.handlers.OutputStringDataFormatterFactory;

import java.util.Map;

abstract class OutputDataStreamSinkHandle implements OutputDataSinkHandle {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;
    private static final int DEFAULT_FLUSH_INTERVAL_MS = 10_000;

    protected int extractBufferSizeProperty(final Map<String, String> properties, final String property) {
        final String bufferSizeStr = properties.get(property);
        return bufferSizeStr == null || bufferSizeStr.isEmpty() ? DEFAULT_BUFFER_SIZE : Integer.parseInt(bufferSizeStr);
    }

    protected long extractFlushIntervalProperty(final Map<String, String> properties, final String property) {
        final String flushIntervalStr = properties.get(property);
        return flushIntervalStr == null || flushIntervalStr.isEmpty() ? DEFAULT_FLUSH_INTERVAL_MS : Long.parseLong(flushIntervalStr);
    }

    protected OutputStringDataFormatter createFormatter(final Map<String, String> properties, final String formatProperty) {
        final String format = properties.get(formatProperty);
        return OutputStringDataFormatterFactory.create(format);
    }
}
