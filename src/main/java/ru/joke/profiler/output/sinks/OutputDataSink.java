package ru.joke.profiler.output.sinks;

import java.util.List;

public interface OutputDataSink<T> extends AutoCloseable {

    default void init() {
    }

    void write(T dataItem);

    default void write(List<T> dataItems) {
        dataItems.forEach(this::write);
    }

    @Override
    default void close() {
    }
}
