package ru.joke.profiler.core.output;

import ru.joke.profiler.core.configuration.DynamicProfilingConfiguration;

abstract class ExecutionTimeRegistrarUtil {

    static boolean isProfilingApplied(
            final String method,
            final long methodElapsedTime,
            final DynamicProfilingConfiguration dynamicConfig) {
        final Thread currentThread = Thread.currentThread();
        return !dynamicConfig.isProfilingDisabled()
                && dynamicConfig.isResourceMustBeProfiled(method)
                && dynamicConfig.getMinExecutionThreshold() <= methodElapsedTime
                && (dynamicConfig.getThreadsFilter() == null || dynamicConfig.getThreadsFilter().test(currentThread.getName()));
    }

    private ExecutionTimeRegistrarUtil() {
    }
}
