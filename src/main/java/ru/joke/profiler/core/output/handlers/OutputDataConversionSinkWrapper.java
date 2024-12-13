package ru.joke.profiler.core.output.handlers;

import java.util.function.Function;

public final class OutputDataConversionSinkWrapper<T> implements OutputDataSink<OutputData> {

    private final OutputDataSink<T> delegate;
    private final Function<OutputData, T> conversionFunc;

    public OutputDataConversionSinkWrapper(
            final OutputDataSink<T> delegate,
            final Function<OutputData, T> conversionFunc) {
        this.conversionFunc = conversionFunc;
        this.delegate = delegate;
    }

    @Override
    public void init() {
        this.delegate.init();
    }

    @Override
    public void close() {
        this.delegate.close();
    }

    @Override
    public void write(final OutputData dataType) {
        final T result = this.conversionFunc.apply(dataType);
        this.delegate.write(result);
    }
}
