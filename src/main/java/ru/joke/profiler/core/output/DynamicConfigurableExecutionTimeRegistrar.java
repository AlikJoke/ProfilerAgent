package ru.joke.profiler.core.output;

import ru.joke.profiler.core.ProfilerException;
import ru.joke.profiler.core.configuration.DynamicProfilingConfiguration;
import ru.joke.profiler.core.configuration.DynamicProfilingConfigurationHolder;
import ru.joke.profiler.core.configuration.StaticProfilingConfiguration;

public final class DynamicConfigurableExecutionTimeRegistrar extends ExecutionTimeRegistrar {

    private final ThreadLocal<ConfigurationWrapper> threadProfilingConfiguration = new ThreadLocal<>();

    private final ExecutionTimeRegistrar delegate;
    private final DynamicProfilingConfigurationHolder dynamicProfilingConfigurationHolder;
    private final StaticProfilingConfiguration staticProfilingConfiguration;

    public DynamicConfigurableExecutionTimeRegistrar(
            final ExecutionTimeRegistrar delegate,
            final StaticProfilingConfiguration staticProfilingConfiguration,
            final DynamicProfilingConfigurationHolder dynamicProfilingConfigurationHolder) {
        this.delegate = delegate;
        this.staticProfilingConfiguration = staticProfilingConfiguration;
        this.dynamicProfilingConfigurationHolder = dynamicProfilingConfigurationHolder;
    }

    @Override
    public void registerMethodEnter(final String method) {
        if (!isTracingRegistrationEnabled()) {
            this.delegate.registerMethodEnter(method);
            return;
        }

        final DynamicProfilingConfiguration dynamicConfig = findDynamicConfig();
        try {
            if (dynamicConfig == null
                    || dynamicConfig.getProfilingRootsFilter() == null
                    || dynamicConfig.getProfilingRootsFilter().test(method)
                    || this.delegate.isRegistrationOccurredOnTrace()) {
                this.delegate.registerMethodEnter(method);
            }
        } finally {
            if (!this.delegate.isRegistrationOccurredOnTrace()) {
                this.threadProfilingConfiguration.remove();
            }
        }
    }

    @Override
    public void registerMethodExit() {
        if (this.isTracingRegistrationEnabled()) {
            this.delegate.registerMethodExit();
            return;
        }

        try {
            if (this.delegate.isRegistrationOccurredOnTrace()) {
                this.delegate.registerMethodExit();
            }
        } finally {
            if (!this.delegate.isRegistrationOccurredOnTrace()) {
                this.threadProfilingConfiguration.remove();
            }
        }
    }

    @Override
    public void registerMethodExit(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime) {

        final boolean isTracingRegistrationEnabled = isTracingRegistrationEnabled();
        if (isTracingRegistrationEnabled && !this.delegate.isRegistrationOccurredOnTrace()) {
            return;
        }

        try {
            final DynamicProfilingConfiguration dynamicConfig = findDynamicConfig();
            if (dynamicConfig == null) {
                this.delegate.registerMethodExit(method, methodEnterTimestamp, methodElapsedTime);
                return;
            }

            if (!isProfiled(dynamicConfig, method)
                    || dynamicConfig.getMinExecutionThreshold() > methodElapsedTime) {
                this.delegate.registerMethodExit();
                return;
            }

            this.delegate.registerMethodExit(method, methodEnterTimestamp, methodElapsedTime);
        } finally {
            if (isTracingRegistrationEnabled && !this.delegate.isRegistrationOccurredOnTrace()) {
                this.threadProfilingConfiguration.remove();
            }
        }
    }

    @Override
    protected void write(String method, long methodEnterTimestamp, long methodElapsedTime) {
        throw new ProfilerException("Method doesn't supported in such type of registrar");
    }

    private boolean isTracingRegistrationEnabled() {
        return this.staticProfilingConfiguration.isExecutionTracingEnabled();
    }

    private boolean isProfiled(final DynamicProfilingConfiguration dynamicConfig, final String method) {
        final Thread currentThread = Thread.currentThread();
        return !dynamicConfig.isProfilingDisabled()
                && dynamicConfig.isResourceMustBeProfiled(method)
                && (dynamicConfig.getThreadsFilter() == null || dynamicConfig.getThreadsFilter().test(currentThread.getName()));
    }

    private DynamicProfilingConfiguration findDynamicConfig() {
        final ConfigurationWrapper profilingConfigurationWrapper = this.threadProfilingConfiguration.get();
        final DynamicProfilingConfiguration dynamicConfig;
        if (profilingConfigurationWrapper == null) {
            dynamicConfig = this.dynamicProfilingConfigurationHolder.getDynamicConfiguration();
            this.threadProfilingConfiguration.set(new ConfigurationWrapper(dynamicConfig));
            return dynamicConfig;
        } else {
            return profilingConfigurationWrapper.configuration;
        }
    }

    private static class ConfigurationWrapper {
        private final DynamicProfilingConfiguration configuration;

        private ConfigurationWrapper(final DynamicProfilingConfiguration configuration) {
            this.configuration = configuration;
        }
    }
}
