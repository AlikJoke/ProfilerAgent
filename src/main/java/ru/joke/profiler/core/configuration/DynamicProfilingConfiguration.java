package ru.joke.profiler.core.configuration;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class DynamicProfilingConfiguration extends ProfilingConfiguration {

    private final boolean profilingEnabled;
    private final Predicate<String> threadsFilter;

    private DynamicProfilingConfiguration(
            final long minExecutionThresholdNs,
            final Predicate<String> resourcesFilter,
            final Predicate<String> threadsFilter,
            final boolean profilingEnabled) {
        super(resourcesFilter, minExecutionThresholdNs);
        this.threadsFilter = threadsFilter;
        this.profilingEnabled = profilingEnabled;
    }

    public boolean isProfilingEnabled() {
        return profilingEnabled;
    }

    public Predicate<String> getThreadsFilter() {
        return threadsFilter;
    }

    @Override
    public String toString() {
        return "DynamicProfilingConfiguration{" + "profilingEnabled=" + profilingEnabled + ", minExecutionThreshold=" + minExecutionThreshold + '}';
    }

    public static DynamicProfilingConfiguration create(
            final long minExecutionThreshold,
            final TimeUnit minExecutionThresholdUnit,
            final Set<String> excludedResources,
            final String excludedResourcesMask,
            final String excludedThreadsMask,
            final boolean profilingEnabled) {

        final Predicate<String> threadsFilter =
                excludedThreadsMask == null || excludedThreadsMask.isEmpty()
                        ? null
                        : Pattern.compile(excludedThreadsMask).asPredicate().negate();
        final Predicate<String> resourcesFilter = composeResourcesFilter(excludedResources, excludedResourcesMask, true);

        return new DynamicProfilingConfiguration(
                minExecutionThresholdUnit.toNanos(minExecutionThreshold),
                resourcesFilter,
                threadsFilter,
                profilingEnabled
        );
    }
}
