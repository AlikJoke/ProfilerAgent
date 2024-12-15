package ru.joke.profiler.core.output.handlers;

import ru.joke.profiler.core.configuration.InvalidConfigurationException;

import java.util.Map;
import java.util.ServiceLoader;

public final class OutputDataSinkFactory {

    public OutputDataSink<OutputData> create(final String type, final Map<String, String> properties) throws Exception {
        for (final OutputDataSinkHandle handle : ServiceLoader.load(OutputDataSinkHandle.class)) {
            if (handle.type().equalsIgnoreCase(type)) {
                return handle.create(properties);
            }
        }

        throw new InvalidConfigurationException(String.format("Unknown sink type provided: %s", type));
    }
}
