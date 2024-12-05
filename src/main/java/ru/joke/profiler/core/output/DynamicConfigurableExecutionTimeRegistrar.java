package ru.joke.profiler.core.output;

import ru.joke.profiler.core.configuration.DynamicProfilingConfiguration;
import ru.joke.profiler.core.configuration.DynamicProfilingConfigurationHolder;

public final class DynamicConfigurableExecutionTimeRegistrar extends ExecutionTimeRegistrar {
    
    private final ExecutionTimeRegistrar delegate;
    private final DynamicProfilingConfigurationHolder dynamicProfilingConfigurationHolder;

    public DynamicConfigurableExecutionTimeRegistrar(
            final ExecutionTimeRegistrar delegate,
            final DynamicProfilingConfigurationHolder dynamicProfilingConfigurationHolder) {
        this.delegate = delegate;
        this.dynamicProfilingConfigurationHolder = dynamicProfilingConfigurationHolder;
    }

    @Override
    public void registerMethodEnter() {
        this.delegate.registerMethodEnter();
    }

    @Override
    public void registerMethodExit() {
        this.delegate.registerMethodExit();
    }

    @Override
    public void registerMethodExit(String method, long methodEnterTimestamp, long methodElapsedTime) {
        final DynamicProfilingConfiguration dynamicConfig = this.dynamicProfilingConfigurationHolder.getDynamicConfiguration();
        if (dynamicConfig == null) {
            registerMethodExit(method, methodEnterTimestamp, methodElapsedTime);
            return;
        }

        if (!isProfilingApplied(method, methodElapsedTime, dynamicConfig)) {
            registerMethodExit();
            return;
        }

        write(method, methodEnterTimestamp, methodElapsedTime);
        registerMethodExit();
    }

    @Override
    protected void write(String method, long methodEnterTimestamp, long methodElapsedTime) {
        this.delegate.write(method, methodEnterTimestamp, methodElapsedTime);
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
