package ru.joke.profiler.core;

import ru.joke.profiler.core.configuration.*;
import ru.joke.profiler.core.transformation.ProfilingTransformer;
import ru.joke.profiler.core.transformation.TransformationFilter;

import java.lang.instrument.Instrumentation;
import java.util.function.Predicate;

public final class ProfilerAgent {

    public static void premain(final String args, final Instrumentation instrumentation) {
        agentmain(args, instrumentation);
    }

    public static void agentmain(final String args, final Instrumentation instrumentation) {
        final ProfilingConfigurationLoader configurationLoader = new ProfilingConfigurationLoader(args);
        final StaticProfilingConfiguration staticConfiguration = configurationLoader.loadStatic();
        final Predicate<String> transformationFilter = new TransformationFilter(staticConfiguration);

        instrumentation.addTransformer(new ProfilingTransformer(transformationFilter, staticConfiguration));

        handleDynamicConfiguration(configurationLoader, staticConfiguration);
    }

    private static void handleDynamicConfiguration(
            final ProfilingConfigurationLoader configurationLoader,
            final StaticProfilingConfiguration staticConfiguration) {
        if (!staticConfiguration.isDynamicConfigurationEnabled()) {
            return;
        }

        final DynamicProfilingConfigurationHolder dynamicConfigHolder = DynamicProfilingConfigurationHolder.getInstance();
        final DynamicProfilingConfigurationRefreshService dynamicConfigRefreshService =
                new DynamicProfilingConfigurationRefreshService(
                        dynamicConfigHolder,
                        configurationLoader,
                        staticConfiguration.getDynamicConfigurationRefreshInterval()
                );
        dynamicConfigRefreshService.run();
    }
}
