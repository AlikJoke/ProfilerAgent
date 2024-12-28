package ru.joke.profiler.output;

import ru.joke.profiler.ProfilerException;
import ru.joke.profiler.output.meta.MethodEnterHandle;
import ru.joke.profiler.output.meta.MethodExitHandle;
import ru.joke.profiler.output.meta.MethodInstanceAccessorHandle;

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
            final long methodElapsedTime
    ) {
        write(method, methodEnterTimestamp, methodElapsedTime);
    }

    protected boolean isRegistrationOccurredOnTrace() {
        return false;
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
