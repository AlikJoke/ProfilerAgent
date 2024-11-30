package ru.joke.profiler.core.configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DynamicProfilingConfigurationRefreshService {

    private static final Logger logger = Logger.getLogger(DynamicProfilingConfigurationRefreshService.class.getCanonicalName());

    private static final String REFRESHING_THREAD_NAME = "profiler-dynamic-configuration-refreshing-thread";
    private static final String SHUTDOWN_THREAD_NAME = "profiler-shutdown-thread";

    private final ScheduledExecutorService executorService;
    private final long dynamicConfigRefreshIntervalMs;
    private final Runnable refreshAction;

    public DynamicProfilingConfigurationRefreshService(
            final DynamicProfilingConfigurationHolder dynamicProfilingConfigurationHolder,
            final ProfilingConfigurationLoader configurationLoader,
            final long dynamicConfigRefreshIntervalMs) {

        this.executorService = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(REFRESHING_THREAD_NAME);
            thread.setUncaughtExceptionHandler((t, e) -> logger.log(Level.SEVERE, "Unable to refresh profiling dynamic configuration", e));

            return thread;
        });

        this.dynamicConfigRefreshIntervalMs = dynamicConfigRefreshIntervalMs;
        this.refreshAction = () -> {
            final DynamicProfilingConfiguration dynamicConfig = configurationLoader.loadDynamic();
            dynamicProfilingConfigurationHolder.setDynamicConfiguration(dynamicConfig);
        };

        Runtime.getRuntime()
                .addShutdownHook(new Thread(this.executorService::shutdownNow, SHUTDOWN_THREAD_NAME));
    }

    public void run() {
        this.refreshAction.run();
        this.executorService.scheduleAtFixedRate(
                this.refreshAction,
                this.dynamicConfigRefreshIntervalMs,
                this.dynamicConfigRefreshIntervalMs,
                TimeUnit.MILLISECONDS
        );
    }
}
