package ru.joke.profiler.core.output;

import ru.joke.profiler.core.ProfilerException;
import ru.joke.profiler.core.configuration.DynamicProfilingConfiguration;
import ru.joke.profiler.core.configuration.DynamicProfilingConfigurationHolder;

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

    @SuppressWarnings("unused")
    public void registerMethodExitDynamic(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime) {
        final DynamicProfilingConfigurationHolder dynamicConfigHolder = DynamicProfilingConfigurationHolder.getInstance();
        final DynamicProfilingConfiguration dynamicConfig = dynamicConfigHolder.getDynamicConfiguration();
        if (dynamicConfig == null) {
            registerMethodExit(method, methodEnterTimestamp, methodElapsedTime);
            return;
        }

        if (!isProfilingApplied(method, methodElapsedTime, dynamicConfig)) {
            registerMethodExit();
            return;
        }

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

    private boolean isProfilingApplied(
            final String method,
            final long methodElapsedTime,
            final DynamicProfilingConfiguration dynamicConfig) {
        final Thread currentThread = Thread.currentThread();
        return !dynamicConfig.isProfilingDisabled()
                && dynamicConfig.isResourceMustBeProfiled(method)
                && dynamicConfig.getMinExecutionThreshold() <= methodElapsedTime
                && (dynamicConfig.getThreadsFilter() == null || dynamicConfig.getThreadsFilter().test(currentThread.getName()));
    }
}
