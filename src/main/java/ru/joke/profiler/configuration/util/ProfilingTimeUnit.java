package ru.joke.profiler.configuration.util;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public enum ProfilingTimeUnit {

    NANOSECONDS("ns", TimeUnit.NANOSECONDS),

    MICROSECONDS("mcs", TimeUnit.MICROSECONDS),

    MILLISECONDS("ms", TimeUnit.MILLISECONDS),

    SECONDS("s", TimeUnit.SECONDS),

    MINUTES("m", TimeUnit.MINUTES),

    HOURS("h", TimeUnit.HOURS);

    private final String alias;
    private final TimeUnit javaTimeUnit;

    ProfilingTimeUnit(final String alias, final TimeUnit javaTimeUnit) {
        this.alias = alias;
        this.javaTimeUnit = javaTimeUnit;
    }

    public TimeUnit toJavaTimeUnit() {
        return this.javaTimeUnit;
    }

    public static ProfilingTimeUnit parse(final String alias, final ProfilingTimeUnit defaultTimeUnit) {
        return Arrays.stream(values())
                        .filter(tu -> tu.alias.equalsIgnoreCase(alias))
                        .findAny()
                        .orElse(defaultTimeUnit);
    }
}
