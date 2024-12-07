package ru.joke.profiler.core.output;

import ru.joke.profiler.core.ProfilerException;
import ru.joke.profiler.core.output.meta.MethodEnterHandle;
import ru.joke.profiler.core.output.meta.MethodExitHandle;
import ru.joke.profiler.core.output.meta.MethodInstanceAccessorHandle;

public abstract class ExecutionTimeRegistrar {

    private static ExecutionTimeRegistrar registrarInstance;

    @SuppressWarnings("unused")
    @MethodInstanceAccessorHandle
    public static ExecutionTimeRegistrar getInstance() {
        return registrarInstance;
    }

    @SuppressWarnings("unused")
    @MethodEnterHandle
    public void registerMethodEnter(final String method) {

    }

    @MethodExitHandle(forTimeRegistration = false)
    public void registerMethodExit() {

    }

    @MethodExitHandle(forTimeRegistration = true)
    public void registerMethodExit(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime) {
        write(method, methodEnterTimestamp, methodElapsedTime);
    }

    protected boolean isRegistrationOccurredOnTrace() {
        throw new ProfilerException("Such type of registrar doesn't support this method");
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
