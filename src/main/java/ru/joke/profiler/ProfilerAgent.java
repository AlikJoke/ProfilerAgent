package ru.joke.profiler;

import ru.joke.profiler.configuration.*;
import ru.joke.profiler.output.ExecutionTimeRegistrar;
import ru.joke.profiler.output.ExecutionTimeRegistrarFactory;
import ru.joke.profiler.output.ExecutionTimeRegistrarMetadataSelector;
import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.OutputDataSink;
import ru.joke.profiler.output.handlers.OutputDataSinkFactory;
import ru.joke.profiler.transformation.*;
import ru.joke.profiler.transformation.spy.SpyContext;
import ru.joke.profiler.transformation.spy.SpyInjector;
import ru.joke.profiler.transformation.spy.SpyInjectorFactory;

import java.lang.instrument.Instrumentation;
import java.util.function.Predicate;

public final class ProfilerAgent {

    public static void premain(final String args, final Instrumentation instrumentation) throws Exception {
        agentmain(args, instrumentation);
    }

    public static void agentmain(final String args, final Instrumentation instrumentation) throws Exception {
        final ProfilingConfigurationLoader configurationLoader = new ProfilingConfigurationLoader(args);
        final StaticProfilingConfiguration staticConfiguration = configurationLoader.loadStatic();

        final DynamicProfilingConfigurationHolder dynamicConfigHolder = createDynamicConfigurationHolder();
        final ExecutionTimeRegistrar registrar = createRegistrar(staticConfiguration, dynamicConfigHolder);

        handleDynamicConfiguration(configurationLoader, staticConfiguration);

        final ProfilingTransformer transformer = createTransformer(
                staticConfiguration,
                registrar,
                dynamicConfigHolder
        );
        instrumentation.addTransformer(transformer);
    }

    private static DynamicProfilingConfigurationHolder createDynamicConfigurationHolder() {
        final DynamicProfilingConfigurationHolderFactory dynamicConfigHolderFactory = new DynamicProfilingConfigurationHolderFactory();
        return dynamicConfigHolderFactory.create();
    }

    private static ProfilingTransformer createTransformer(
            final StaticProfilingConfiguration configuration,
            final ExecutionTimeRegistrar registrar,
            final DynamicProfilingConfigurationHolder dynamicConfigHolder
    ) {
        final Predicate<String> transformationFilter = new TransformationFilter(configuration);
        final ExecutionTimeRegistrarMetadataSelector registrarMetadataSelector = new ExecutionTimeRegistrarMetadataSelector(ExecutionTimeRegistrar.class);
        final NativeClassMethodsCollector nativeClassMethodsCollector = new NativeClassMethodsCollector(transformationFilter);
        final SpyInjector spyInjector = createSpyInjector(
                configuration,
                registrar,
                dynamicConfigHolder
        );

        return new ProfilingTransformer(
                transformationFilter,
                configuration,
                registrarMetadataSelector,
                nativeClassMethodsCollector,
                spyInjector
        );
    }

    private static SpyInjector createSpyInjector(
            final StaticProfilingConfiguration staticConfiguration,
            final ExecutionTimeRegistrar registrar,
            final DynamicProfilingConfigurationHolder dynamicConfigHolder
    ) {
        final SpyContext context = new SpyContext() {
            @Override
            public ExecutionTimeRegistrar registrar() {
                return registrar;
            }

            @Override
            public StaticProfilingConfiguration staticConfiguration() {
                return staticConfiguration;
            }

            @Override
            public DynamicProfilingConfigurationHolder dynamicConfigurationHolder() {
                return dynamicConfigHolder;
            }
        };
        return SpyInjectorFactory.create(context);
    }

    private static ExecutionTimeRegistrar createRegistrar(
            final StaticProfilingConfiguration staticConfiguration,
            final DynamicProfilingConfigurationHolder dynamicConfigHolder
    ) throws Exception {
        final OutputDataSink<OutputData> sink = createOutputSink(staticConfiguration);
        Runtime.getRuntime()
                .addShutdownHook(new Thread(sink::close));

        final ExecutionTimeRegistrarFactory registrarFactory = new ExecutionTimeRegistrarFactory(
                staticConfiguration,
                dynamicConfigHolder,
                sink
        );
        return registrarFactory.create();
    }

    private static OutputDataSink<OutputData> createOutputSink(final StaticProfilingConfiguration configuration) throws Exception {
        final OutputDataSinkFactory sinkFactory = new OutputDataSinkFactory();
        final OutputDataSink<OutputData> sink = sinkFactory.create(
                configuration.sinks(),
                configuration.ignoreSinkErrors(),
                configuration.sinkProperties()
        );
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
