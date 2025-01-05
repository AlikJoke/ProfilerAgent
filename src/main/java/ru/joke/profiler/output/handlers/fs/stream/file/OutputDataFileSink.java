package ru.joke.profiler.output.handlers.fs.stream.file;

import ru.joke.profiler.output.handlers.fs.stream.OutputDataAbsStreamSink;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

final class OutputDataFileSink extends OutputDataAbsStreamSink<FileSinkConfiguration> {

    OutputDataFileSink(final FileSinkConfiguration configuration) throws IOException {
        super(
                createWriter(configuration),
                configuration
        );
    }

    private static Writer createWriter(final FileSinkConfiguration configuration) throws IOException {
        final Writer writer = configuration.existingFilePolicy().createWriter(configuration.filePath());
        return configuration.rotation().enabled()
                ? new RotatableBufferedWriter(writer, configuration)
                : new BufferedWriter(writer, configuration.bufferSize());
    }
}
