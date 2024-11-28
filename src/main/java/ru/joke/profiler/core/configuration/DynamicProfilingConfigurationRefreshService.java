package ru.joke.profiler.core.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.joke.profiler.core.configuration.ConfigurationProperties.DYNAMIC_CONFIGURATION_FILEPATH;

public final class DynamicProfilingConfigurationRefreshService {

    private static final Logger logger = Logger.getLogger(DynamicProfilingConfigurationRefreshService.class.getCanonicalName());

    private static final String REFRESHING_THREAD_NAME = "profiler-dynamic-configuration-refreshing-thread";
    private static final String SHUTDOWN_THREAD_NAME = "profiler-shutdown-thread";

    private final ScheduledExecutorService executorService;
    private final long dynamicConfigRefreshIntervalMs;
    private final Runnable refreshAction;

    public DynamicProfilingConfigurationRefreshService(
            final DynamicProfilingConfigurationHolder dynamicProfilingConfigurationHolder,
            final String dynamicConfigFilePath,
            final long dynamicConfigRefreshIntervalMs) {
        if (dynamicConfigFilePath == null || dynamicConfigFilePath.isEmpty()) {
            throw new InvalidConfigurationException(
                    String.format("File path for dynamic configuration is not set; check '%s' arg property of the javaagent", DYNAMIC_CONFIGURATION_FILEPATH));
        }

        this.executorService = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(REFRESHING_THREAD_NAME);
            thread.setUncaughtExceptionHandler((t, e) -> logger.log(Level.SEVERE, "Unable to refresh profiling dynamic configuration", e));

            return thread;
        });

        this.dynamicConfigRefreshIntervalMs = dynamicConfigRefreshIntervalMs;
        this.refreshAction = () -> {
            final DynamicProfilingConfiguration configuration = loadConfiguration(dynamicConfigFilePath);
            dynamicProfilingConfigurationHolder.setDynamicConfiguration(configuration);
        };

        Runtime.getRuntime()
                .addShutdownHook(new Thread(this.executorService::shutdownNow, SHUTDOWN_THREAD_NAME));
    }

    public void start() {
        // immediately load dynamic configuration at start
        this.refreshAction.run();
        this.executorService.scheduleAtFixedRate(
                this.refreshAction,
                this.dynamicConfigRefreshIntervalMs,
                this.dynamicConfigRefreshIntervalMs,
                TimeUnit.MILLISECONDS
        );
    }

    private DynamicProfilingConfiguration loadConfiguration(final String configFilePath) {
        final Path configurationFilePath = Paths.get(configFilePath);
        final File configurationFile = configurationFilePath.toFile();
        if (!validateConfigFile(configurationFile)) {
            return null;
        }

        try (final InputStream fis = Files.newInputStream(configurationFilePath, StandardOpenOption.READ)) {
            final Properties props = new Properties();
            props.load(fis);

            return DynamicProfilingConfiguration.create(props);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean validateConfigFile(final File configurationFile) {
        if (!configurationFile.exists()) {
            logger.warning(String.format("Configuration file does not exist: %s", configurationFile.getAbsolutePath()));
            return false;
        }

        if (!configurationFile.canRead()) {
            logger.warning(String.format("Read permission denied for file '%s'", configurationFile.getAbsolutePath()));
            return false;
        }

        if (!configurationFile.isFile()) {
            logger.warning(String.format("The path to the configuration file does not refer to a file: '%s'", configurationFile.getAbsolutePath()));
            return false;
        }

        return true;
    }
}
