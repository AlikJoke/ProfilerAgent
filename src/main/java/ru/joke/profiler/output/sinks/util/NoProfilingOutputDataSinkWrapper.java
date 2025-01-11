package ru.joke.profiler.output.sinks.util;

import ru.joke.profiler.output.sinks.OutputDataSink;
import ru.joke.profiler.transformation.ProfilingTransformer;

import java.util.List;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

public final class NoProfilingOutputDataSinkWrapper<T> extends OutputDataSink<T> {

    private final OutputDataSink<T> delegate;

    public NoProfilingOutputDataSinkWrapper(final OutputDataSink<T> delegate) {
        this.delegate = checkNotNull(delegate, "delegate");
    }

    @Override
    public void init() {
        this.delegate.init();
    }

    @Override
    public void write(final T dataItem) {
        ProfilingTransformer.disable();
        try {
            this.delegate.write(dataItem);
        } finally {
            ProfilingTransformer.enable();
        }
    }

    @Override
    public void write(final List<T> dataItems) {
        ProfilingTransformer.disable();
        try {
            this.delegate.write(dataItems);
        } finally {
            ProfilingTransformer.enable();
        }
    }

    @Override
    public void close() {
        this.delegate.close();
    }

    @Override
    public String toString() {
        return "NoProfilingOutputDataSinkWrapper{" + "delegate=" + delegate + '}';
    }
}
