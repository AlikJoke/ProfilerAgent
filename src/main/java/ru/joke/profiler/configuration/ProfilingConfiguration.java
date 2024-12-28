package ru.joke.profiler.configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

abstract class ProfilingConfiguration {

    static final String MIN_EXECUTION_THRESHOLD = "min_execution_threshold";
    static final String EXCLUDED_RESOURCES = "excluded_resources";
    static final String EXCLUDED_RESOURCES_MASK = "excluded_resources_mask";

    protected final Predicate<String> resourcesFilter;
    protected final long minExecutionThresholdNs;

    ProfilingConfiguration(
            final Predicate<String> resourcesFilter,
            final long minExecutionThresholdNs
    ) {
        this.resourcesFilter = resourcesFilter;
        this.minExecutionThresholdNs = minExecutionThresholdNs;
    }

    public long minExecutionThresholdNs() {
        return this.minExecutionThresholdNs;
    }

    public boolean isResourceMustBeProfiled(final String resourceName) {
        return this.resourcesFilter == null || this.resourcesFilter.test(resourceName);
    }

    static Predicate<String> composeResourcesFilter(
            final String includedResources,
            final String includedResourcesMask,
            final String excludedResources,
            final String excludedResourcesMask,
            final char delimiter
    ) {
        final Set<String> includedResourcesSet = includedResources == null ? Collections.emptySet() : parseResourcesArg(includedResources, delimiter);
        final Set<String> excludedResourcesSet = excludedResources == null ? Collections.emptySet() : parseResourcesArg(excludedResources, delimiter);

        final Predicate<String> resourcesFilterByExcluded = composeResourcesFilter(excludedResourcesSet, excludedResourcesMask, true);
        final Predicate<String> resourcesFilterByIncluded = composeResourcesFilter(includedResourcesSet, includedResourcesMask, false);

        return resourcesFilterByExcluded == null
                ? resourcesFilterByIncluded
                : resourcesFilterByIncluded == null
                    ? resourcesFilterByExcluded
                    : resourcesFilterByIncluded.and(resourcesFilterByExcluded);
    }

    private static Set<String> parseResourcesArg(final String arg, final char delimiter) {
        return Arrays.stream(arg.split(","))
                        .filter(p -> !p.isEmpty())
                        .map(p -> p.replace('.', delimiter))
                        .collect(Collectors.toSet());
    }

    private static Predicate<String> composeResourcesFilter(
            final Set<String> explicitResources,
            final String resourcesMask,
            final boolean forExclusion
    ) {
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
