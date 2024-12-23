package ru.joke.profiler.output.handlers;

import ru.joke.profiler.ProfilerException;

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
