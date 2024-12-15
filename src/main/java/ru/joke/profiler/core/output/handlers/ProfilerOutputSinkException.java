package ru.joke.profiler.core.output.handlers;

import ru.joke.profiler.core.ProfilerException;

public final class ProfilerOutputSinkException extends ProfilerException {

    public ProfilerOutputSinkException(Throwable cause) {
        super(cause);
    }

    public ProfilerOutputSinkException(String message) {
        super(message);
    }

    public ProfilerOutputSinkException(String message, Throwable cause) {
        super(message, cause);
    }
}
