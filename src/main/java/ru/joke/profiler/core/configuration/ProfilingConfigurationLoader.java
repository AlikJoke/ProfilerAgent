package ru.joke.profiler.core.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Logger;

import static ru.joke.profiler.core.configuration.ConfigurationProperties.CONFIGURATION_FILE_PATH_ARG;

public final class ProfilingConfigurationLoader {

    private static final Logger logger = Logger.getLogger(ProfilingConfigurationLoader.class.getCanonicalName());

    private final String configurationFilePath;

    public ProfilingConfigurationLoader(final String agentArgs) {
        if (agentArgs == null || agentArgs.isEmpty()) {
            this.configurationFilePath = null;
        } else {
            this.configurationFilePath =
                    Arrays.stream(agentArgs.split(";"))
                            .map(arg -> arg.split("="))
                            .filter(arg -> arg.length > 1 && !arg[1].isEmpty())
                            .filter(arg -> CONFIGURATION_FILE_PATH_ARG.equalsIgnoreCase(arg[0]))
                            .map(arg -> arg[1])
                            .findAny()
                            .orElse(null);
        }

        if (this.configurationFilePath != null) {
            logger.info(String.format("Provided configuration file: %s", this.configurationFilePath));
        } else {
            logger.info("Configuration file doesn't provided, will be applied default configuration");
        }
    }

    public DynamicProfilingConfiguration loadDynamic() {
        if (this.configurationFilePath == null) {
            return null;
        }

        return loadByConfigFile(DynamicProfilingConfiguration::create);
    }

    public StaticProfilingConfiguration loadStatic() {
        if (this.configurationFilePath == null) {
            return new StaticProfilingConfiguration(
                    null,
                    0,
                    false,
                    0,
                    false
            );
        }

        return loadByConfigFile(StaticProfilingConfiguration::create);
    }

    private <T> T loadByConfigFile(final Function<Properties, T> configurationFunc) {

        final Path configurationFilePath = Paths.get(this.configurationFilePath);
        final File configurationFile = configurationFilePath.toFile();
        if (!validateConfigFile(configurationFile)) {
            return null;
        }

        try (final InputStream fis = Files.newInputStream(configurationFilePath, StandardOpenOption.READ)) {
            final Properties props = new Properties(16);
            props.load(fis);

            return configurationFunc.apply(props);
        } catch (IOException e) {
            throw new InvalidConfigurationException(String.format("Unable to create configuration from file %s", this.configurationFilePath), e);
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
