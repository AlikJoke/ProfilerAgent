package ru.joke.profiler.configuration;

import ru.joke.profiler.configuration.loaders.ProfilingConfigurationLoader;
import ru.joke.profiler.util.ProfilerThreadFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class DynamicProfilingConfigurationRefreshService {

    private static final Logger logger = Logger.getLogger(DynamicProfilingConfigurationRefreshService.class.getCanonicalName());

    private static final String REFRESHING_THREAD_NAME = "profiler-dynamic-configuration-refreshing-thread";

    private final ScheduledExecutorService executorService;
    private final long dynamicConfigRefreshIntervalMs;
    private final Runnable refreshAction;

    public DynamicProfilingConfigurationRefreshService(
            final DynamicProfilingConfigurationHolder dynamicProfilingConfigurationHolder,
            final ProfilingConfigurationLoader configurationLoader,
            final long dynamicConfigRefreshIntervalMs
    ) {
        this.executorService = Executors.newSingleThreadScheduledExecutor(new ProfilerThreadFactory(REFRESHING_THREAD_NAME, false));
        this.dynamicConfigRefreshIntervalMs = dynamicConfigRefreshIntervalMs;
        this.refreshAction = () -> {
            final DynamicProfilingConfiguration dynamicConfig = configurationLoader.loadDynamic();
            dynamicProfilingConfigurationHolder.set(dynamicConfig);
        };
    }

    public void start() {
        logger.info("Dynamic configuration refresh service will be started");

        this.refreshAction.run();
        this.executorService.scheduleAtFixedRate(
                this.refreshAction,
                this.dynamicConfigRefreshIntervalMs,
                this.dynamicConfigRefreshIntervalMs,
                TimeUnit.MILLISECONDS
        );

        logger.info("Dynamic configuration refresh service started");
    }

    public void close() {
        logger.info("Dynamic configuration refresh service will be closed");
        this.executorService.shutdownNow();
        logger.info("Dynamic configuration refresh service closed");
    }
}
