package ru.joke.profiler.configuration.util;

import ru.joke.profiler.configuration.meta.StatelessParser;

@StatelessParser
public final class NanoTimePropertyParser extends AbstractTimePropertyParser {

    public NanoTimePropertyParser() {
        super(ProfilingTimeUnit.NANOSECONDS);
    }
}
