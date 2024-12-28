package ru.joke.profiler.output.handlers.fs.stream.file;

import ru.joke.profiler.output.handlers.fs.stream.OutputDataAbsStreamSink;

import java.io.IOException;

final class OutputDataFileSink extends OutputDataAbsStreamSink<FileSinkConfiguration> {

    OutputDataFileSink(final FileSinkConfiguration configuration) throws IOException {
        super(
                configuration.existingFilePolicy().createWriter(configuration.filePath()),
                configuration
        );
    }
}
