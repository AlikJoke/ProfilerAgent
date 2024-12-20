package ru.joke.profiler.core.output.handlers.kafka;

enum ProcessingInRecoveryStatePolicy {

    SKIP,

    WAIT,

    ERROR;

    static ProcessingInRecoveryStatePolicy parse(final String alias) {
        for (final ProcessingInRecoveryStatePolicy policy : values()) {
            if (policy.name().equalsIgnoreCase(alias)) {
                return policy;
            }
        }

        return SKIP;
    }
}
