package ru.joke.profiler.core.configuration;

import java.util.function.Predicate;

abstract class ProfilingConfiguration {

    protected final Predicate<String> resourcesFilter;
    protected final long minExecutionThreshold;

    ProfilingConfiguration(final Predicate<String> resourcesFilter, final long minExecutionThreshold) {
        this.resourcesFilter = resourcesFilter;
        this.minExecutionThreshold = minExecutionThreshold;
    }

    public long getMinExecutionThreshold() {
        return this.minExecutionThreshold;
    }

    public boolean isResourceMustBeProfiled(final String resourceName) {
        return this.resourcesFilter == null || this.resourcesFilter.test(resourceName);
    }
}
