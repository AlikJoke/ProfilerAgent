package ru.joke.profiler.core.output;

import ru.joke.profiler.core.ProfilerException;

public abstract class ExecutionTimeRegistrar {

    private static ExecutionTimeRegistrar registrarInstance;

    @SuppressWarnings("unused")
    public static ExecutionTimeRegistrar getInstance() {
        return registrarInstance;
    }

    @SuppressWarnings("unused")
    public void registerMethodEnter() {

    }

    public void registerMethodExit() {

    }

    public void registerMethodExit(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime) {
        write(method, methodEnterTimestamp, methodElapsedTime);
    }

    protected abstract void write(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime
    );

    void init() {
        if (registrarInstance != null) {
            throw new ProfilerException("Registrar instance already created: " + registrarInstance);
        }

        registrarInstance = this;
    }
}
