package ru.joke.profiler.output.sinks.util;

import ru.joke.profiler.output.sinks.OutputData;
import ru.joke.profiler.output.sinks.OutputDataSink;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

public final class OutputDataConversionSinkWrapper<T> extends OutputDataSink<OutputData> {

    private final OutputDataSink<T> delegate;
    private final Function<OutputData, T> conversionFunc;

    public OutputDataConversionSinkWrapper(
            final OutputDataSink<T> delegate,
            final Function<OutputData, T> conversionFunc
    ) {
        this.conversionFunc = checkNotNull(conversionFunc, "conversionFunc");
        this.delegate = checkNotNull(delegate, "delegate");
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
    public void write(final OutputData dataItem) {
        final T result = this.conversionFunc.apply(dataItem);
        this.delegate.write(result);
    }

    @Override
    public void write(final List<OutputData> dataItems) {
        final List<T> result =
                dataItems
                        .stream()
                        .map(this.conversionFunc)
                        .collect(Collectors.toList());
        this.delegate.write(result);
    }
}
