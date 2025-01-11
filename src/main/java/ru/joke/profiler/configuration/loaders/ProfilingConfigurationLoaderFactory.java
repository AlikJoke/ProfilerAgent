package ru.joke.profiler.configuration.loaders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

public final class ProfilingConfigurationLoaderFactory {

    public ProfilingConfigurationLoader create(final String args) {

        final Map<String, String> argsMap = parseArgs(checkNotNull(args, "args"));
        final ConfigurationPropertiesLoader[] loaders = createPropertiesLoaders(argsMap);

        final ConfigurationPropertiesLoader compositePropertiesLoader = () -> {
            final Map<String, String> result = new HashMap<>();
            for (final ConfigurationPropertiesLoader loader : loaders) {
                result.putAll(loader.load());
            }

            return result;
        };

        return new ProfilingConfigurationLoader(compositePropertiesLoader);
    }

    private Map<String, String> parseArgs(final String args) {
        return Arrays.stream(args.split(";"))
                        .map(arg -> arg.split("="))
                        .filter(arg -> arg.length > 1 && !arg[1].isEmpty())
                        .collect(Collectors.toMap(arg -> arg[0].toLowerCase(), arg -> arg[1]));
    }

    private ConfigurationPropertiesLoader[] createPropertiesLoaders(final Map<String, String> args) {
        final ConfigurationPropertiesLoader filePropertiesLoader = new FileConfigurationPropertiesLoader(args);
        final ConfigurationPropertiesLoader vaultPropertiesLoader = createVaultPropertiesLoader(args, filePropertiesLoader);

        return new ConfigurationPropertiesLoader[] { filePropertiesLoader, vaultPropertiesLoader };
    }

    private ConfigurationPropertiesLoader createVaultPropertiesLoader(
            final Map<String, String> args,
            final ConfigurationPropertiesLoader filePropertiesLoader
    ) {
        final Map<String, String> result = new HashMap<>(args);
        result.putAll(filePropertiesLoader.load());

        return new VaultConfigurationPropertiesLoader(result);
    }
}
