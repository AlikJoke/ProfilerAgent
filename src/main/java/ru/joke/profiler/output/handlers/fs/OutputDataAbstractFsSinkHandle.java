package ru.joke.profiler.output.handlers.fs;

import ru.joke.profiler.configuration.meta.ConfigurationParser;
import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.OutputDataSink;
import ru.joke.profiler.output.handlers.async.AsyncOutputDataSinkHandleSupport;
import ru.joke.profiler.output.handlers.util.OutputDataConversionSinkWrapper;
import ru.joke.profiler.output.handlers.util.injectors.OutputStringDataFormatter;
import ru.joke.profiler.output.handlers.util.injectors.OutputStringDataFormatterFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class OutputDataAbstractFsSinkHandle<C extends AbstractFsSinkConfiguration> extends AsyncOutputDataSinkHandleSupport<String> {

    protected static final String FORMATTER_KEY = "formatter";
    protected static final String CONFIG_KEY = "config";

    @Override
    protected Function<OutputData, Supplier<String>> conversionFunction(
            final Map<String, String> properties,
            final Map<String, Object> context
    ) {
        final OutputStringDataFormatter formatter = (OutputStringDataFormatter) context.get(FORMATTER_KEY);
        return formatter::formatLater;
    }

    @Override
    protected OutputDataSink<OutputData> createSyncOutputSink(
            final Map<String, String> properties,
            final Map<String, Object> context
    ) throws Exception {
        final OutputStringDataFormatter formatter = (OutputStringDataFormatter) context.get(FORMATTER_KEY);
        return new OutputDataConversionSinkWrapper<>(
                createTerminalOutputSink(properties, context),
                formatter::format
        );
    }

    @Override
    protected Map<String, Object> buildCreationContext(final Map<String, String> properties) {
        final C sinkConfiguration = ConfigurationParser.parse(configurationType(), properties);
        final OutputStringDataFormatter formatter = OutputStringDataFormatterFactory.create(sinkConfiguration.outputDataPattern());

        final Map<String, Object> context = new HashMap<>(2, 1);
        context.put(CONFIG_KEY, sinkConfiguration);
        context.put(FORMATTER_KEY, formatter);

        return Collections.unmodifiableMap(context);
    }

    protected C getConfiguration(final Map<String, Object> context) {
        @SuppressWarnings("unchecked")
        final C config = (C) context.get(CONFIG_KEY);
        return config;
    }

    protected abstract Class<C> configurationType();
}
