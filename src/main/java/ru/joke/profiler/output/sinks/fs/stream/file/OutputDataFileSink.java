package ru.joke.profiler.output.sinks.fs.stream.file;

import ru.joke.profiler.output.sinks.fs.stream.OutputDataAbsStreamSink;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

final class OutputDataFileSink extends OutputDataAbsStreamSink<FileSinkConfiguration> {

    OutputDataFileSink(final FileSinkConfiguration configuration) throws IOException {
        super(
                createWriter(checkNotNull(configuration, "configuration")),
                configuration
        );
    }

    private static Writer createWriter(final FileSinkConfiguration configuration) throws IOException {
        final Writer writer = configuration.existingFilePolicy().createWriter(configuration.filePath());
        final FileSinkConfiguration.Rotation rotation = configuration.rotation();
        return rotation != null && rotation.enabled()
                ? new RotatableBufferedWriter(writer, configuration)
                : new BufferedWriter(writer, configuration.bufferSize());
    }
}
