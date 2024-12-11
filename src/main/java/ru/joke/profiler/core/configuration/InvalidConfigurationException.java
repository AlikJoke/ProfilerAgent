package ru.joke.profiler.core.configuration;

import ru.joke.profiler.core.ProfilerException;

public final class InvalidConfigurationException extends ProfilerException {

    public InvalidConfigurationException(final String message) {
        super(message);
    }

    public InvalidConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
