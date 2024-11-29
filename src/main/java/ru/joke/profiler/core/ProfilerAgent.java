package ru.joke.profiler.core;

import ru.joke.profiler.core.configuration.DynamicProfilingConfigurationHolder;
import ru.joke.profiler.core.configuration.DynamicProfilingConfigurationRefreshService;
import ru.joke.profiler.core.configuration.StaticProfilingConfiguration;
import ru.joke.profiler.core.transformation.ProfilingTransformer;
import ru.joke.profiler.core.transformation.TransformationFilter;

import java.lang.instrument.Instrumentation;
import java.util.function.Predicate;

public final class ProfilerAgent {

    public static void premain(final String args, final Instrumentation instrumentation) {
        agentmain(args, instrumentation);
    }

    public static void agentmain(final String args, final Instrumentation instrumentation) {
        final StaticProfilingConfiguration configuration = StaticProfilingConfiguration.parse(args);
        final Predicate<String> transformationFilter = new TransformationFilter(configuration);

        instrumentation.addTransformer(new ProfilingTransformer(transformationFilter, configuration));

        startDynamicConfigurationRefreshingIfNeed(configuration);
    }

    private static void startDynamicConfigurationRefreshingIfNeed(final StaticProfilingConfiguration configuration) {
        if (!configuration.isDynamicConfigurationEnabled()) {
            return;
        }

        final DynamicProfilingConfigurationHolder dynamicConfigHolder = DynamicProfilingConfigurationHolder.getInstance();
        final DynamicProfilingConfigurationRefreshService dynamicConfigRefreshService =
                new DynamicProfilingConfigurationRefreshService(
                        dynamicConfigHolder,
                        configuration.getDynamicConfigurationFilePath(),
                        configuration.getDynamicConfigurationRefreshInterval()
                );
        dynamicConfigRefreshService.start();
    }
}
