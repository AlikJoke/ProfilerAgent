package ru.joke.profiler.configuration.loaders;

import ru.joke.profiler.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

final class FileConfigurationPropertiesLoader implements ConfigurationPropertiesLoader {

    private static final Logger logger = Logger.getLogger(ConfigurationPropertiesLoader.class.getCanonicalName());

    private static final String CONFIGURATION_FILE_PATH_ARG = "conf_file";

    private final String configurationFilePath;

    FileConfigurationPropertiesLoader(final Map<String, String> args) {
        this.configurationFilePath = args == null || args.isEmpty() ? null : args.get(CONFIGURATION_FILE_PATH_ARG);

        if (this.configurationFilePath != null) {
            logger.info(String.format("Provided configuration file: %s", this.configurationFilePath));
        } else {
            logger.info("Configuration file doesn't provided, will be applied default configuration");
        }
    }

    @Override
    public Map<String, String> load() {
        final Path configurationFilePath = Paths.get(this.configurationFilePath);
        final File configurationFile = configurationFilePath.toFile();
        if (!validateConfigFile(configurationFile)) {
            return Collections.emptyMap();
        }

        try (final InputStream fis = Files.newInputStream(configurationFilePath, StandardOpenOption.READ)) {
            final Properties props = new Properties();
            props.load(fis);

            return props.stringPropertyNames()
                            .stream()
                            .filter(p -> props.getProperty(p) != null && !props.getProperty(p).isEmpty())
                            .collect(Collectors.toMap(Function.identity(), props::getProperty));
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
