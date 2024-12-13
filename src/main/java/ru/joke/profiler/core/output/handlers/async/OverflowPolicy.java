package ru.joke.profiler.core.output.handlers.async;

public enum OverflowPolicy {

    SYNC,

    DISCARD,

    WAIT,

    ERROR;

    public static OverflowPolicy parse(final String alias) {
        for (final OverflowPolicy policy : values()) {
            if (policy.name().equalsIgnoreCase(alias)) {
                return policy;
            }
        }

        return SYNC;
    }
}
