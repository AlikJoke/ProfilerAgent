package ru.joke.profiler.output.sinks.fs.stream.console;

import ru.joke.profiler.output.sinks.fs.stream.OutputDataAbsStreamSink;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

final class OutputDataConsoleSink extends OutputDataAbsStreamSink<ConsoleSinkConfiguration> {

    OutputDataConsoleSink(final ConsoleSinkConfiguration configuration) throws UnsupportedEncodingException {
        super(
                new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8.displayName()), configuration.bufferSize()),
                checkNotNull(configuration, "configuration")
        );
    }
}
