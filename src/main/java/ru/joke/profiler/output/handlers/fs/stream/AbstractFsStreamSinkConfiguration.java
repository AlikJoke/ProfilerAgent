package ru.joke.profiler.output.handlers.fs.stream;

import ru.joke.profiler.output.handlers.fs.AbstractFsSinkConfiguration;

public abstract class AbstractFsStreamSinkConfiguration extends AbstractFsSinkConfiguration {

    protected static final String OUTPUT_STREAM_BUFFER_SIZE = "output_stream_buffer_size";
    protected static final String OUTPUT_STREAM_FORCE_FLUSH_ON_WRITES = "output_stream_force_flush_on_writes";

    protected final int bufferSize;
    protected final boolean forceFlushOnWrites;

    protected AbstractFsStreamSinkConfiguration(
            final String outputDataPattern,
            final int bufferSize,
            final boolean forceFlushOnWrites
    ) {
        super(outputDataPattern);
        this.bufferSize = bufferSize;
        this.forceFlushOnWrites = forceFlushOnWrites;
    }

    public int bufferSize() {
        return bufferSize;
    }

    public boolean forceFlushOnWrites() {
        return forceFlushOnWrites;
    }
}
