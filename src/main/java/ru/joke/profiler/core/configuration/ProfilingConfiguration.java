package ru.joke.profiler.core.configuration;

import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

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

    static Predicate<String> composeResourcesFilter(
            final Set<String> explicitResources,
            final String resourcesMask,
            final boolean forExclusion) {
        final Predicate<String> resourcesByMaskFilter =
                resourcesMask == null || resourcesMask.isEmpty()
                        ? null
                        : Pattern.compile(resourcesMask).asPredicate();

        final Predicate<String> resultResourcesByMaskFilter =
                resourcesByMaskFilter != null && forExclusion
                        ? resourcesByMaskFilter.negate()
                        : resourcesByMaskFilter;

        final Predicate<String> explicitResourcesFilter =
                r -> forExclusion
                        ? explicitResources.stream().noneMatch(r::startsWith)
                        : explicitResources.stream().anyMatch(r::startsWith);
        return explicitResources.isEmpty() && resultResourcesByMaskFilter == null
                ? null
                : explicitResources.isEmpty()
                    ? resultResourcesByMaskFilter
                    : resultResourcesByMaskFilter == null
                        ? explicitResourcesFilter
                        : forExclusion
                            ? resultResourcesByMaskFilter.and(explicitResourcesFilter)
                            : resultResourcesByMaskFilter.or(explicitResourcesFilter);
    }
}
