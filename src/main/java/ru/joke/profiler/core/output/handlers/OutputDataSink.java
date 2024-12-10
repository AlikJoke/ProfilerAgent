package ru.joke.profiler.core.output.handlers;

public interface OutputDataSink extends AutoCloseable {

    default void init() {
    }

    void write(OutputData outputData);

    @Override
    default void close() {
    }
}
