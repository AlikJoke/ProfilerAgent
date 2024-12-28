package ru.joke.profiler;

import ru.joke.profiler.configuration.*;
import ru.joke.profiler.output.ExecutionTimeRegistrar;
import ru.joke.profiler.output.ExecutionTimeRegistrarInitializer;
import ru.joke.profiler.output.ExecutionTimeRegistrarMetadataSelector;
import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.OutputDataSink;
import ru.joke.profiler.output.handlers.OutputDataSinkFactory;
import ru.joke.profiler.transformation.ProfilingTransformer;
import ru.joke.profiler.transformation.TransformationFilter;

import java.lang.instrument.Instrumentation;
import java.util.function.Predicate;

public final class ProfilerAgent {

    public static void premain(final String args, final Instrumentation instrumentation) throws Exception {
        agentmain(args, instrumentation);
    }

    public static void agentmain(final String args, final Instrumentation instrumentation) throws Exception {
        final ProfilingConfigurationLoader configurationLoader = new ProfilingConfigurationLoader(args);
        final StaticProfilingConfiguration staticConfiguration = configurationLoader.loadStatic();

        initializeRegistrar(staticConfiguration);
        final ExecutionTimeRegistrarMetadataSelector registrarMetadataSelector = new ExecutionTimeRegistrarMetadataSelector(ExecutionTimeRegistrar.class);
        final Predicate<String> transformationFilter = new TransformationFilter(staticConfiguration);

        handleDynamicConfiguration(configurationLoader, staticConfiguration);

        instrumentation.addTransformer(new ProfilingTransformer(transformationFilter, staticConfiguration, registrarMetadataSelector));
    }

    private static void initializeRegistrar(final StaticProfilingConfiguration staticConfiguration) throws Exception {
        final DynamicProfilingConfigurationHolderFactory dynamicConfigHolderFactory = new DynamicProfilingConfigurationHolderFactory();
        final DynamicProfilingConfigurationHolder dynamicConfigHolder = dynamicConfigHolderFactory.create();

        final OutputDataSink<OutputData> sink = createOutputSink(staticConfiguration);
        Runtime.getRuntime()
                .addShutdownHook(new Thread(sink::close));

        final ExecutionTimeRegistrarInitializer registrarInitializer = new ExecutionTimeRegistrarInitializer(
                staticConfiguration,
                dynamicConfigHolder,
                sink
        );
        registrarInitializer.init();
    }

    private static OutputDataSink<OutputData> createOutputSink(final StaticProfilingConfiguration configuration) throws Exception {
        final OutputDataSinkFactory sinkFactory = new OutputDataSinkFactory();
        final OutputDataSink<OutputData> sink = sinkFactory.create(configuration.sinkType(), configuration.sinkProperties());
        tryInitSink(sink);

        return sink;
    }

    private static void tryInitSink(final OutputDataSink<OutputData> sink) {
        try {
            sink.init();
        } catch (RuntimeException ex) {
            sink.close();
            throw ex;
        }
    }

    private static void handleDynamicConfiguration(
            final ProfilingConfigurationLoader configurationLoader,
            final StaticProfilingConfiguration staticConfiguration
    ) {
        if (!staticConfiguration.dynamicConfigurationEnabled()) {
            return;
        }

        final DynamicProfilingConfigurationHolder dynamicConfigHolder = DynamicProfilingConfigurationHolder.getInstance();
        final DynamicProfilingConfigurationRefreshService dynamicConfigRefreshService =
                new DynamicProfilingConfigurationRefreshService(
                        dynamicConfigHolder,
                        configurationLoader,
                        staticConfiguration.dynamicConfigurationRefreshIntervalMs()
                );
        dynamicConfigRefreshService.start();

        Runtime.getRuntime()
                .addShutdownHook(new Thread(dynamicConfigRefreshService::close));
    }
}
