package ru.joke.profiler.core.output.handlers.jdbc;

import ru.joke.profiler.core.configuration.InvalidConfigurationException;

enum ExistingTablePolicy {

    RECREATE,

    NONE,

    TRUNCATE;

    static ExistingTablePolicy parse(final String alias) {
        for (final ExistingTablePolicy policy : values()) {
            if (policy.name().equals(alias)) {
                return policy;
            }
        }

        if (alias == null || alias.isEmpty()) {
            return NONE;
        }

        throw new InvalidConfigurationException("Unknown type of policy: " + alias);
    }
}
