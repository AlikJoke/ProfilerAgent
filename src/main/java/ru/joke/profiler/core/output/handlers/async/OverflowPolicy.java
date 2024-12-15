package ru.joke.profiler.core.output.handlers.async;

enum OverflowPolicy {

    SYNC,

    DISCARD,

    WAIT,

    ERROR;

    static OverflowPolicy parse(final String alias) {
        for (final OverflowPolicy policy : values()) {
            if (policy.name().equalsIgnoreCase(alias)) {
                return policy;
            }
        }

        return SYNC;
    }
}
