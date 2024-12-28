package ru.joke.profiler.output.handlers.fs.stream.console;

import ru.joke.profiler.output.handlers.fs.stream.OutputDataAbsStreamSink;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

final class OutputDataConsoleSink extends OutputDataAbsStreamSink<ConsoleSinkConfiguration> {

    OutputDataConsoleSink(final ConsoleSinkConfiguration configuration) throws UnsupportedEncodingException {
        super(
                new OutputStreamWriter(System.out, StandardCharsets.UTF_8.displayName()),
                configuration
        );
    }
}
