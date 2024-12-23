package ru.joke.profiler;

public class ProfilerException extends RuntimeException {

    public ProfilerException(final Throwable cause) {
        super(cause);
    }

    public ProfilerException(final String message) {
        super(message);
    }

    public ProfilerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
