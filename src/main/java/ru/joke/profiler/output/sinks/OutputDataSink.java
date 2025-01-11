package ru.joke.profiler.output.sinks;

import java.util.List;
import java.util.logging.Logger;

public abstract class OutputDataSink<T> implements AutoCloseable {

    protected final Logger logger = Logger.getLogger(getClass().getCanonicalName());

    public void init() {
        logger.info(String.format("Sink %s initialized", this));
    }

    public abstract void write(T dataItem);

    public void write(List<T> dataItems) {
        dataItems.forEach(this::write);
    }

    @Override
    public void close() {
        logger.info(String.format("Sink %s closed", this));
    }
}
