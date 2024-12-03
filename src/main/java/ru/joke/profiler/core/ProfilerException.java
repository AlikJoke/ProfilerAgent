package ru.joke.profiler.core;

public final class ProfilerException extends RuntimeException {

    public ProfilerException(final Throwable cause) {
        super(cause);
    }

    public ProfilerException(final String message) {
        super(message);
    }
}
