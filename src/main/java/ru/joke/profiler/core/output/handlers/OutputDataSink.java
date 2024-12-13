package ru.joke.profiler.core.output.handlers;

public interface OutputDataSink<T> extends AutoCloseable {

    default void init() {
    }

    void write(T dataType);

    @Override
    default void close() {
    }
}
