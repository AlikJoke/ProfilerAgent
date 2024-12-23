package ru.joke.profiler.output.handlers.jul;

import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.OutputDataSink;
import ru.joke.profiler.output.handlers.async.AsyncOutputDataSinkHandleSupport;
import ru.joke.profiler.output.handlers.util.NoProfilingOutputDataSinkWrapper;
import ru.joke.profiler.output.handlers.util.OutputDataConversionSinkWrapper;
import ru.joke.profiler.output.handlers.util.OutputStringDataFormatter;
import ru.joke.profiler.output.handlers.util.OutputStringDataFormatterFactory;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static ru.joke.profiler.configuration.ConfigurationProperties.*;

public final class OutputDataLoggerSinkHandle extends AsyncOutputDataSinkHandleSupport<String> {

    public static final String SINK_TYPE = "logger";
    private static final String FORMATTER_KEY = "formatter";

    @Override
    public String type() {
        return SINK_TYPE;
    }

    @Override
    protected Function<OutputData, Supplier<String>> conversionFunction(
            final Map<String, String> properties,
            final Map<String, Object> context) {
        final OutputStringDataFormatter formatter = (OutputStringDataFormatter) context.get(FORMATTER_KEY);
        return formatter::formatLater;
    }

    @Override
    protected OutputDataSink<String> createTerminalOutputSink(
            final Map<String, String> properties,
            final Map<String, Object> context) {
        final OutputDataSink<String> terminalSink = new OutputDataLoggerSink(
                findRequiredProperty(properties, STATIC_LOGGER_SINK_CATEGORY),
                findRequiredProperty(properties, STATIC_LOGGER_SINK_LEVEL)
        );

        return new NoProfilingOutputDataSinkWrapper<>(terminalSink);
    }

    @Override
    protected OutputDataSink<OutputData> createSyncOutputSink(
            final Map<String, String> properties,
            final Map<String, Object> context) {
        final OutputStringDataFormatter formatter = (OutputStringDataFormatter) context.get(FORMATTER_KEY);
        return new OutputDataConversionSinkWrapper<>(
                createTerminalOutputSink(properties, context),
                formatter::format
        );
    }

    @Override
    protected Map<String, Object> buildCreationContext(Map<String, String> properties) {
        final String format = properties.get(STATIC_LOGGER_SINK_DATA_FORMAT);
        final OutputStringDataFormatter formatter = OutputStringDataFormatterFactory.create(format);
        return Collections.singletonMap(FORMATTER_KEY, formatter);
    }
}
