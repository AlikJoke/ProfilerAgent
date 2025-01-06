package ru.joke.profiler.output.sinks.fs;

import ru.joke.profiler.output.sinks.OutputData;
import ru.joke.profiler.output.sinks.OutputDataSink;
import ru.joke.profiler.output.sinks.async.AsyncOutputDataSinkHandleSupport;
import ru.joke.profiler.output.sinks.util.OutputDataConversionSinkWrapper;
import ru.joke.profiler.output.sinks.util.injectors.OutputStringDataFormatter;
import ru.joke.profiler.output.sinks.util.injectors.OutputStringDataFormatterFactory;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class OutputDataAbstractFsSinkHandle<C extends AbstractFsSinkConfiguration> extends AsyncOutputDataSinkHandleSupport<String, C> {

    protected static final String FORMATTER_KEY = "formatter";
    protected static final String CONFIG_KEY = "config";

    @Override
    protected Function<OutputData, Supplier<String>> conversionFunction(
            final C configuration,
            final Map<String, Object> context
    ) {
        final OutputStringDataFormatter formatter = (OutputStringDataFormatter) context.get(FORMATTER_KEY);
        return formatter::formatLater;
    }

    @Override
    protected OutputDataSink<OutputData> createSyncOutputSink(
            final C configuration,
            final Map<String, Object> context
    ) throws Exception {
        final OutputStringDataFormatter formatter = (OutputStringDataFormatter) context.get(FORMATTER_KEY);
        return new OutputDataConversionSinkWrapper<>(
                createTerminalOutputSink(configuration, context),
                formatter::format
        );
    }

    @Override
    protected Map<String, Object> buildCreationContext(final C configuration) {
        final OutputStringDataFormatter formatter = OutputStringDataFormatterFactory.create(configuration.outputDataPattern());
        return Collections.singletonMap(FORMATTER_KEY, formatter);
    }
}
