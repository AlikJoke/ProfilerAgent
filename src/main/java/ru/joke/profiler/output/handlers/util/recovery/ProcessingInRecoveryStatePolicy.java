package ru.joke.profiler.output.handlers.util.recovery;

import ru.joke.profiler.configuration.InvalidConfigurationException;

public enum ProcessingInRecoveryStatePolicy {

    SKIP,

    WAIT,

    ERROR;

    public static ProcessingInRecoveryStatePolicy parse(final String alias) {
        for (final ProcessingInRecoveryStatePolicy policy : values()) {
            if (policy.name().equalsIgnoreCase(alias)) {
                return policy;
            }
        }

        if (alias == null || alias.isEmpty()) {
            return WAIT;
        }

        throw new InvalidConfigurationException("Unknown policy alias: " + alias);
    }
}
