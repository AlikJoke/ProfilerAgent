package ru.joke.profiler.output.handlers.stream;

import ru.joke.profiler.configuration.ConfigurationProperties;
import ru.joke.profiler.output.handlers.util.OutputStringDataFormatter;
import ru.joke.profiler.output.handlers.util.OutputStringDataFormatterFactory;
import ru.joke.profiler.output.handlers.async.AsyncOutputDataSinkHandleSupport;

import java.util.Collections;
import java.util.Map;

public abstract class OutputDataStreamSinkHandle extends AsyncOutputDataSinkHandleSupport<String> {

    protected static final String FORMATTER_KEY = "formatter";
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;

    private final String formatterProperty;

    protected OutputDataStreamSinkHandle(final String formatterProperty) {
        this.formatterProperty = formatterProperty;
    }

    protected int extractBufferSizeProperty(final Map<String, String> properties, final String property) {
        return ConfigurationProperties.parseIntProperty(properties, property, DEFAULT_BUFFER_SIZE);
    }

    protected OutputStringDataFormatter getFormatter(final Map<String, Object> context) {
        return (OutputStringDataFormatter) context.get(FORMATTER_KEY);
    }

    @Override
    protected Map<String, Object> buildCreationContext(final Map<String, String> properties) {
        final String format = properties.get(this.formatterProperty);
        final OutputStringDataFormatter formatter = OutputStringDataFormatterFactory.create(format);
        return Collections.singletonMap(FORMATTER_KEY, formatter);
    }
}
