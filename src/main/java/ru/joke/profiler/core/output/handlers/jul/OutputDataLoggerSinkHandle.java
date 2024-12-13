package ru.joke.profiler.core.output.handlers.jul;

import ru.joke.profiler.core.configuration.InvalidConfigurationException;
import ru.joke.profiler.core.output.handlers.*;
import ru.joke.profiler.core.output.handlers.async.AsyncOutputDataSinkHandleSupport;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static ru.joke.profiler.core.configuration.ConfigurationProperties.*;

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
        return new OutputDataLoggerSink(
                findRequiredPropertyValue(properties, STATIC_LOGGER_SINK_CATEGORY),
                findRequiredPropertyValue(properties, STATIC_LOGGER_SINK_LEVEL)
        );
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

    private String findRequiredPropertyValue(final Map<String, String> properties, final String propertyName) {
        final String propertyValue = properties.get(propertyName);
        if (propertyValue == null || propertyValue.isEmpty()) {
            throw new InvalidConfigurationException(String.format("Property (%s) is required for logger sink", propertyName));
        }

        return propertyValue;
    }
}
