package ru.joke.profiler.core.output.handlers.kafka;

public enum ProcessingInRecoveryStatePolicy {

    SKIP,

    WAIT,

    ERROR
}
