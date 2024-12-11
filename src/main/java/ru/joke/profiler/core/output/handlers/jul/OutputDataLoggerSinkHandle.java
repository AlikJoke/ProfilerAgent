package ru.joke.profiler.core.output.handlers.jul;

import ru.joke.profiler.core.configuration.InvalidConfigurationException;
import ru.joke.profiler.core.output.handlers.OutputDataSink;
import ru.joke.profiler.core.output.handlers.OutputDataSinkHandle;
import ru.joke.profiler.core.output.handlers.OutputStringDataFormatterFactory;

import java.util.Map;

import static ru.joke.profiler.core.configuration.ConfigurationProperties.*;

public final class OutputDataLoggerSinkHandle implements OutputDataSinkHandle {

    public static final String SINK_TYPE = "logger";

    @Override
    public String type() {
        return SINK_TYPE;
    }

    @Override
    public OutputDataSink create(final Map<String, String> properties) throws Exception {
        final String format = properties.get(STATIC_LOGGER_SINK_DATA_FORMAT);
        return new OutputDataLoggerSink(
                OutputStringDataFormatterFactory.create(format),
                findRequiredPropertyValue(properties, STATIC_LOGGER_SINK_CATEGORY),
                findRequiredPropertyValue(properties, STATIC_LOGGER_SINK_LEVEL)
        );
    }

    private String findRequiredPropertyValue(final Map<String, String> properties, final String propertyName) {
        final String propertyValue = properties.get(propertyName);
        if (propertyValue == null || propertyValue.isEmpty()) {
            throw new InvalidConfigurationException(String.format("Property (%s) is required for logger sink", propertyName));
        }

        return propertyValue;
    }
}
