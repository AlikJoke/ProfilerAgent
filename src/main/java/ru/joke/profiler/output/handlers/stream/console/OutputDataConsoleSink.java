package ru.joke.profiler.output.handlers.stream.console;

import ru.joke.profiler.output.handlers.stream.OutputDataAbsStreamSink;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public final class OutputDataConsoleSink extends OutputDataAbsStreamSink {

    public OutputDataConsoleSink(final int bufferSize, final boolean forceFlushOnWrites) throws UnsupportedEncodingException {
        super(
                new OutputStreamWriter(System.out, StandardCharsets.UTF_8.displayName()),
                bufferSize,
                forceFlushOnWrites
        );
    }
}
