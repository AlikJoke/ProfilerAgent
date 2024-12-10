package ru.joke.profiler.core.output.handlers.stream;

import ru.joke.profiler.core.output.handlers.OutputStringDataFormatter;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class OutputDataFileSink extends OutputDataAbsStreamSink {

    public OutputDataFileSink(
            final OutputStringDataFormatter formatter,
            final int bufferSize,
            final long flushInterval,
            final String filePath) throws FileNotFoundException, UnsupportedEncodingException {
        super(new PrintStream(filePath, StandardCharsets.UTF_8.displayName()), formatter, bufferSize, flushInterval);
    }
}
