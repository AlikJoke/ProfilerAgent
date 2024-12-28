package ru.joke.profiler.output;

import ru.joke.profiler.configuration.DynamicProfilingConfigurationHolder;
import ru.joke.profiler.ProfilerException;
import ru.joke.profiler.configuration.DynamicProfilingConfiguration;
import ru.joke.profiler.configuration.StaticProfilingConfiguration;

public final class DynamicConfigurableExecutionTimeRegistrar extends ExecutionTimeRegistrar {

    private final ThreadLocal<DynamicExecutionContext> threadExecutionContext = new ThreadLocal<>();

    private final ExecutionTimeRegistrar delegate;
    private final DynamicProfilingConfigurationHolder dynamicProfilingConfigurationHolder;
    private final StaticProfilingConfiguration staticProfilingConfiguration;

    public DynamicConfigurableExecutionTimeRegistrar(
            final ExecutionTimeRegistrar delegate,
            final StaticProfilingConfiguration staticProfilingConfiguration,
            final DynamicProfilingConfigurationHolder dynamicProfilingConfigurationHolder
    ) {
        this.delegate = delegate;
        this.staticProfilingConfiguration = staticProfilingConfiguration;
        this.dynamicProfilingConfigurationHolder = dynamicProfilingConfigurationHolder;
    }

    @Override
    public void registerMethodEnter(final String method) {

        final DynamicExecutionContext executionContext = findOrCreateExecutionContext();
        if (executionContext.configuration != null && ++executionContext.depth > executionContext.configuration.profiledTraceMaxDepth()) {
            return;
        }

        try {
            if (!isTracingRegistrationEnabled()) {
                this.delegate.registerMethodEnter(method);
                return;
            }

            if ((executionContext.configuration == null
                    || executionContext.configuration.profilingRootsFilter() == null
                    || executionContext.configuration.profilingRootsFilter().test(method)
                    || this.delegate.isRegistrationOccurredOnTrace())) {
                this.delegate.registerMethodEnter(method);
            }
        } finally {
            if (!this.delegate.isRegistrationOccurredOnTrace()) {
                this.threadExecutionContext.remove();
            }
        }
    }

    @Override
    public void registerMethodExit() {
        final DynamicExecutionContext executionContext = findOrCreateExecutionContext();
        try {
            if (executionContext.configuration != null && executionContext.depth-- > executionContext.configuration.profiledTraceMaxDepth()) {
                return;
            }

            if (!isTracingRegistrationEnabled()) {
                this.delegate.registerMethodExit();
                return;
            }

            if (this.delegate.isRegistrationOccurredOnTrace()) {
                this.delegate.registerMethodExit();
            }
        } finally {
            if (executionContext.depth == 0) {
                this.threadExecutionContext.remove();
            }
        }
    }

    @Override
    public void registerMethodExit(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime
    ) {
        final DynamicExecutionContext executionContext = findOrCreateExecutionContext();
        try {
            if (executionContext.configuration != null && executionContext.depth-- > executionContext.configuration.profiledTraceMaxDepth()
                    || isTracingRegistrationEnabled() && !this.delegate.isRegistrationOccurredOnTrace()) {
                return;
            }

            if (executionContext.configuration == null) {
                this.delegate.registerMethodExit(method, methodEnterTimestamp, methodElapsedTime);
                return;
            }

            if (!isProfiled(executionContext.configuration, method)
                    || executionContext.configuration.minExecutionThresholdNs() > methodElapsedTime) {
                this.delegate.registerMethodExit();
                return;
            }

            this.delegate.registerMethodExit(method, methodEnterTimestamp, methodElapsedTime);
        } finally {
            if (executionContext.depth == 0) {
                this.threadExecutionContext.remove();
            }
        }
    }

    @Override
    protected void write(String method, long methodEnterTimestamp, long methodElapsedTime) {
        throw new ProfilerException("Method doesn't supported in such type of registrar");
    }

    @Override
    protected boolean isRegistrationOccurredOnTrace() {
        return this.delegate.isRegistrationOccurredOnTrace();
    }

    private boolean isTracingRegistrationEnabled() {
        return this.staticProfilingConfiguration.executionTracingEnabled();
    }

    private boolean isProfiled(final DynamicProfilingConfiguration dynamicConfig, final String method) {
        final Thread currentThread = Thread.currentThread();
        return !dynamicConfig.profilingDisabled()
                && dynamicConfig.isResourceMustBeProfiled(method)
                && (dynamicConfig.threadsFilter() == null || dynamicConfig.threadsFilter().test(currentThread.getName()));
    }

    private DynamicExecutionContext findOrCreateExecutionContext() {
        final DynamicExecutionContext context = this.threadExecutionContext.get();
        if (context == null) {
            final DynamicProfilingConfiguration dynamicConfig = this.dynamicProfilingConfigurationHolder.getDynamicConfiguration();
            final DynamicExecutionContext result = new DynamicExecutionContext(dynamicConfig);
            this.threadExecutionContext.set(result);
            return result;
        } else {
            return context;
        }
    }

    private static class DynamicExecutionContext {

        private final DynamicProfilingConfiguration configuration;
        private int depth;

        private DynamicExecutionContext(final DynamicProfilingConfiguration configuration) {
            this.configuration = configuration;
        }
    }
}
