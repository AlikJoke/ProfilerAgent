package ru.joke.profiler.core.configuration;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

abstract class ConfigurationProperties {

    private static final String STATIC_PREFIX = "static.";
    private static final String DYNAMIC_PREFIX = "dynamic.";
    
    private static final String ABS_MIN_EXECUTION_THRESHOLD = "min_execution_threshold";
    private static final String ABS_MIN_EXECUTION_THRESHOLD_TU = "min_execution_threshold_tu";
    private static final String ABS_EXCLUDED_RESOURCES = "excluded_resources";
    private static final String ABS_EXCLUDED_RESOURCES_MASK = "excluded_resources_mask";

    static final String STATIC_MIN_EXECUTION_THRESHOLD = createStaticProperty(ABS_MIN_EXECUTION_THRESHOLD);
    static final String STATIC_MIN_EXECUTION_THRESHOLD_TU = createStaticProperty(ABS_MIN_EXECUTION_THRESHOLD_TU);
    static final String STATIC_INCLUDED_RESOURCES = createStaticProperty("included_resources");
    static final String STATIC_INCLUDED_RESOURCES_MASK = createStaticProperty("included_resources_mask");
    static final String STATIC_EXCLUDED_RESOURCES = createStaticProperty(ABS_EXCLUDED_RESOURCES);
    static final String STATIC_EXCLUDED_RESOURCES_MASK = createStaticProperty(ABS_EXCLUDED_RESOURCES_MASK);
    static final String STATIC_DYNAMIC_CONFIGURATION_ENABLED = createStaticProperty("dynamic_conf_enabled");
    static final String STATIC_DYNAMIC_CONFIGURATION_REFRESH_INTERVAL = createStaticProperty("dynamic_conf_refresh_interval");
    static final String STATIC_DYNAMIC_CONFIGURATION_REFRESH_INTERVAL_TU = createStaticProperty("dynamic_conf_refresh_interval_tu");
    static final String STATIC_EXECUTION_TRACING_ENABLED = createStaticProperty("execution_tracing_enabled");

    static final String DYNAMIC_MIN_EXECUTION_THRESHOLD = createDynamicProperty(ABS_MIN_EXECUTION_THRESHOLD);
    static final String DYNAMIC_MIN_EXECUTION_THRESHOLD_TU = createDynamicProperty(ABS_MIN_EXECUTION_THRESHOLD_TU);
    static final String DYNAMIC_EXCLUDED_RESOURCES = createDynamicProperty(ABS_EXCLUDED_RESOURCES);
    static final String DYNAMIC_EXCLUDED_RESOURCES_MASK = createDynamicProperty(ABS_EXCLUDED_RESOURCES_MASK);
    static final String DYNAMIC_EXCLUDED_THREADS_MASK = createDynamicProperty("excluded_threads_mask");
    static final String DYNAMIC_PROFILING_DISABLED = createDynamicProperty("profiling_disabled");

    static final String CONFIGURATION_FILE_PATH_ARG = "conf_file";

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
    
    static String createStaticProperty(final String property) {
        return createProperty(STATIC_PREFIX, property);
    }

    static String createDynamicProperty(final String property) {
        return createProperty(DYNAMIC_PREFIX, property);
    }
    
    private static String createProperty(final String prefix, final String property) {
        return prefix + property;
    }
}