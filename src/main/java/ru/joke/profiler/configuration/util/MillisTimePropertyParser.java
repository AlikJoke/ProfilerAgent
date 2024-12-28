package ru.joke.profiler.configuration.util;

import ru.joke.profiler.configuration.meta.StatelessParser;

@StatelessParser
public final class MillisTimePropertyParser extends AbstractTimePropertyParser {

    public MillisTimePropertyParser() {
        super(ProfilingTimeUnit.MILLISECONDS);
    }
}
