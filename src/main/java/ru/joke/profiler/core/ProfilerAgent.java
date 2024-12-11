package ru.joke.profiler.core;

import ru.joke.profiler.core.configuration.*;
import ru.joke.profiler.core.output.ExecutionTimeRegistrar;
import ru.joke.profiler.core.output.ExecutionTimeRegistrarInitializer;
import ru.joke.profiler.core.output.ExecutionTimeRegistrarMetadataSelector;
import ru.joke.profiler.core.output.handlers.OutputDataSink;
import ru.joke.profiler.core.output.handlers.OutputDataSinkFactory;
import ru.joke.profiler.core.transformation.ProfilingTransformer;
import ru.joke.profiler.core.transformation.TransformationFilter;

import java.lang.instrument.Instrumentation;
import java.util.function.Predicate;

public final class ProfilerAgent {

    @SuppressWarnings("unused")
    public static void premain(final String args, final Instrumentation instrumentation) throws Exception {
        agentmain(args, instrumentation);
    }

    public static void agentmain(final String args, final Instrumentation instrumentation) throws Exception {
        final ProfilingConfigurationLoader configurationLoader = new ProfilingConfigurationLoader(args);
        final StaticProfilingConfiguration staticConfiguration = configurationLoader.loadStatic();
        final Predicate<String> transformationFilter = new TransformationFilter(staticConfiguration);

        initializeRegistrar(staticConfiguration);
        final ExecutionTimeRegistrarMetadataSelector registrarMetadataSelector = new ExecutionTimeRegistrarMetadataSelector(ExecutionTimeRegistrar.class);

        instrumentation.addTransformer(new ProfilingTransformer(transformationFilter, staticConfiguration, registrarMetadataSelector));

        handleDynamicConfiguration(configurationLoader, staticConfiguration);
    }

    private static void initializeRegistrar(final StaticProfilingConfiguration staticConfiguration) throws Exception {
        final DynamicProfilingConfigurationHolderFactory dynamicConfigHolderFactory = new DynamicProfilingConfigurationHolderFactory();
        final DynamicProfilingConfigurationHolder dynamicConfigHolder = dynamicConfigHolderFactory.create();

        final OutputDataSink sink = createOutputSink(staticConfiguration);

        final ExecutionTimeRegistrarInitializer registrarInitializer = new ExecutionTimeRegistrarInitializer(staticConfiguration, dynamicConfigHolder, sink);
        registrarInitializer.init();
    }

    private static OutputDataSink createOutputSink(final StaticProfilingConfiguration configuration) throws Exception {
        final OutputDataSinkFactory sinkFactory = new OutputDataSinkFactory();
        final OutputDataSink sink = sinkFactory.create(configuration.getSinkType(), configuration.getSinkProperties());
        sink.init();

        return sink;
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
        dynamicConfigRefreshService.start();
    }
}
