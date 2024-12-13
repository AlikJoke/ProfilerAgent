package ru.joke.profiler.core.output.handlers.stream;

import ru.joke.profiler.core.ProfilerException;
import ru.joke.profiler.core.output.handlers.OutputDataSink;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class OutputDataAbsStreamSink implements OutputDataSink<String> {

    private static final Logger logger = Logger.getLogger(OutputDataSink.class.getCanonicalName());

    private final Writer writer;
    private final boolean forceFlushOnWrites;

    protected OutputDataAbsStreamSink(
            final Writer writer,
            final int bufferSize,
            final boolean forceFlushOnWrites) {
        this.writer = new BufferedWriter(writer, bufferSize);
        this.forceFlushOnWrites = forceFlushOnWrites;
    }

    @Override
    public void write(final String outputData) {
        try {
            this.writer.write(outputData);
            this.writer.write(System.lineSeparator());

            if (this.forceFlushOnWrites) {
                this.writer.flush();
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Unable to write to sink", ex);
            throw new ProfilerException(ex);
        }
    }

    @Override
    public void close() {
        try {
            this.writer.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Unable to close sink", ex);
            throw new ProfilerException(ex);
        }
    }
}
