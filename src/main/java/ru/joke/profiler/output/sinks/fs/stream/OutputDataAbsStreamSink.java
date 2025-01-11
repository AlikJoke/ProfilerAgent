package ru.joke.profiler.output.sinks.fs.stream;

import ru.joke.profiler.output.sinks.OutputDataSink;
import ru.joke.profiler.output.sinks.ProfilerOutputSinkException;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.logging.Level;

public abstract class OutputDataAbsStreamSink<C extends AbstractFsStreamSinkConfiguration> extends OutputDataSink<String> {

    private final Writer writer;
    private final boolean forceFlushOnWrites;
    private volatile boolean isClosed;

    protected OutputDataAbsStreamSink(
            final Writer writer,
            final C configuration
    ) {
        this.writer = writer;
        this.forceFlushOnWrites = configuration.forceFlushOnWrites();

        logger.info("Sink created with configuration: " + configuration);
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
        logger.info("Closing stream sink");
        try {
            this.isClosed = true;
            this.writer.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Unable to close sink", ex);
            throw new ProfilerOutputSinkException(ex);
        }

        super.close();
    }
}
