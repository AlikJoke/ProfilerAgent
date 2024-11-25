package ru.joke.profiler.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public final class ProfilingConfiguration {

    private final Set<String> targetPackages;

    public ProfilingConfiguration(final String args) {
        this.targetPackages =
                args == null || args.isEmpty()
                        ? Collections.emptySet()
                        : Arrays.stream(args.split(";"))
                                .map(p -> p.replace(".", "/"))
                                .collect(Collectors.toSet());
    }

    public Set<String> getTargetPackages() {
        return this.targetPackages;
    }
}
