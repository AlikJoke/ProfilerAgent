package ru.joke.profiler.core.output.handlers.stream;

import ru.joke.profiler.core.output.handlers.OutputDataSink;
import ru.joke.profiler.core.output.handlers.ProfilerOutputSinkException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class OutputDataAbsStreamSink implements OutputDataSink<String> {

    private static final Logger logger = Logger.getLogger(OutputDataSink.class.getCanonicalName());

    private final Writer writer;
    private final boolean forceFlushOnWrites;
    private volatile boolean isClosed;

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
            if (this.isClosed) {
                return;
            }

            logger.log(Level.SEVERE, "Unable to write to sink", ex);
            throw new ProfilerOutputSinkException(ex);
        }
    }

    @Override
    public void write(final List<String> dataItems) {
        final String result = String.join(System.lineSeparator(), dataItems);
        write(result);
    }

    @Override
    public synchronized void close() {
        try {
            this.isClosed = true;
            this.writer.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Unable to close sink", ex);
            throw new ProfilerOutputSinkException(ex);
        }
    }
}
