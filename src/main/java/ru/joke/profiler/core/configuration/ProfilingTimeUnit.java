package ru.joke.profiler.core.configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

enum ProfilingTimeUnit {

    NANOSECONDS("ns", TimeUnit.NANOSECONDS),

    SECONDS("s", TimeUnit.SECONDS),

    MILLISECONDS("ms", TimeUnit.MILLISECONDS),

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
