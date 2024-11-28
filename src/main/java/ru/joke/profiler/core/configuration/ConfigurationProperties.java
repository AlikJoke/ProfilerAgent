package ru.joke.profiler.core.configuration;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

abstract class ConfigurationProperties {

    static final String MIN_EXECUTION_THRESHOLD = "min_execution_threshold";
    static final String MIN_EXECUTION_THRESHOLD_TU = "min_execution_threshold_tu";
    static final String INCLUDED_RESOURCES = "included_resources";
    static final String INCLUDED_RESOURCES_MASK = "included_resources_mask";
    static final String EXCLUDED_RESOURCES = "excluded_resources";
    static final String EXCLUDED_RESOURCES_MASK = "excluded_resources_mask";
    
    static final String EXCLUDED_THREADS_MASK = "excluded_threads_mask";
    static final String PROFILING_DISABLED = "profiling_disabled";
    
    static final String DYNAMIC_CONFIGURATION_ENABLED = "dynamic_conf_enabled";
    static final String DYNAMIC_CONFIGURATION_FILEPATH = "dynamic_conf_file";
    static final String DYNAMIC_CONFIGURATION_REFRESH_INTERVAL = "dynamic_conf_refresh_interval";
    static final String DYNAMIC_CONFIGURATION_REFRESH_INTERVAL_TU = "dynamic_conf_refresh_interval_tu";

    static long parseExecutionThreshold(final String thresholdArg, final String thresholdTimeUnitArg) {
        final long minExecutionThreshold = thresholdArg == null || thresholdArg.isEmpty() ? 0 : Long.parseLong(thresholdArg);
        final TimeUnit minExecThresholdTimeUnit = ProfilingTimeUnit.parse(thresholdTimeUnitArg, ProfilingTimeUnit.NANOSECONDS).toJavaTimeUnit();

        return minExecThresholdTimeUnit.toNanos(minExecutionThreshold);
    }

    static Set<String> parseResourcesArg(final String arg, final char delimiter) {
        return Arrays.stream(arg.split(","))
                        .filter(p -> !p.isEmpty())
                        .map(p -> p.replace('.', delimiter))
                        .collect(Collectors.toSet());
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
