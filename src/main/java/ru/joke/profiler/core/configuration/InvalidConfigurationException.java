package ru.joke.profiler.core.configuration;

public final class InvalidConfigurationException extends RuntimeException {

    public InvalidConfigurationException(final String message) {
        super(message);
    }

    public InvalidConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
