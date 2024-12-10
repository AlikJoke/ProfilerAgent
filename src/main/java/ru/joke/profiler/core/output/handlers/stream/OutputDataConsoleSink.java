package ru.joke.profiler.core.output.handlers.stream;

import ru.joke.profiler.core.output.handlers.OutputStringDataFormatter;

public final class OutputDataConsoleSink extends OutputDataAbsStreamSink {

    public OutputDataConsoleSink(
            final OutputStringDataFormatter formatter,
            final int bufferSize,
            final long flushInterval) {
        super(System.out, formatter, bufferSize, flushInterval);
    }
}
