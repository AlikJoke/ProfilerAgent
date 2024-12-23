package ru.joke.profiler.output.handlers.stream.file;

import ru.joke.profiler.output.handlers.stream.OutputDataAbsStreamSink;

import java.io.IOException;

public final class OutputDataFileSink extends OutputDataAbsStreamSink {

    public OutputDataFileSink(
            final int bufferSize,
            final ExistingFilePolicy existingFilePolicy,
            final String filePath,
            final boolean forceFlushOnWrites) throws IOException {
        super(
                existingFilePolicy.createWriter(filePath),
                bufferSize,
                forceFlushOnWrites
        );
    }
}
