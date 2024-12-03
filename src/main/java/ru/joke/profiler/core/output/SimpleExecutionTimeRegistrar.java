package ru.joke.profiler.core.output;

import ru.joke.profiler.core.configuration.DynamicProfilingConfiguration;
import ru.joke.profiler.core.configuration.DynamicProfilingConfigurationHolder;

import static ru.joke.profiler.core.output.ExecutionTimeRegistrarUtil.isProfilingApplied;

public final class SimpleExecutionTimeRegistrar {

    public static void registerStatic(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime) {
        write(method, methodEnterTimestamp, methodElapsedTime);
    }

    @SuppressWarnings("unused")
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

        if (!isProfilingApplied(method, methodElapsedTime, dynamicConfig)) {
            return;
        }

        write(method, methodEnterTimestamp, methodElapsedTime);
    }

    private static void write(final String method, final long methodEnterTimestamp, final long methodElapsedTime) {
        // TODO
        System.out.println(method + ":" + methodEnterTimestamp + ":" + methodElapsedTime);
    }
}
