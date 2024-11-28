package ru.joke.profiler.core.output;

import ru.joke.profiler.core.configuration.DynamicProfilingConfiguration;
import ru.joke.profiler.core.configuration.DynamicProfilingConfigurationHolder;

public final class ExecutionTimeRegistrar {

    public static void registerStatic(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime) {
        // TODO
        System.out.println(method + ":" + methodEnterTimestamp + ":" + methodElapsedTime);
    }

    public static void registerDynamic(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime) {
        final DynamicProfilingConfigurationHolder dynamicConfigHolder = DynamicProfilingConfigurationHolder.getInstance();
        final DynamicProfilingConfiguration dynamicConfig = dynamicConfigHolder.getDynamicConfiguration();
        if (dynamicConfig == null) {
            registerStatic(method, methodEnterTimestamp, methodElapsedTime);
            return;
        }

        final Thread currentThread = Thread.currentThread();
        if (dynamicConfig.isProfilingDisabled()
                || !dynamicConfig.isResourceMustBeProfiled(method)
                || dynamicConfig.getMinExecutionThreshold() > methodElapsedTime
                || dynamicConfig.getThreadsFilter() != null && !dynamicConfig.getThreadsFilter().test(currentThread.getName())) {
            return;
        }

        // TODO
        System.out.println(method + ":" + methodEnterTimestamp + ":" + methodElapsedTime);
    }
}
