package ru.joke.profiler.configuration;

import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.util.MapConfigurationPropertiesParser;
import ru.joke.profiler.configuration.util.NanoTimePropertyParser;

import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class DynamicProfilingConfiguration extends ProfilingConfiguration {

    private static final String DYNAMIC_PREFIX = "dynamic.";
    private static final String EXCLUDED_THREADS_MASK = "excluded_threads_mask";
    private static final String PROFILING_DISABLED = "profiling_disabled";
    private static final String PROFILING_ROOTS = "profiling_roots";
    private static final String PROFILING_ROOTS_MASK = "profiling_roots_mask";
    private static final String PROFILED_STACKTRACE_MAX_DEPTH = "profiled_stacktrace_max_depth";
    private static final String SPY_PREFIX = "spy.";

    private final boolean profilingDisabled;
    private final Predicate<String> threadsFilter;
    private final Predicate<String> profilingRootsFilter;
    private final int profiledTraceMaxDepth;
    private final Map<String, String> spiesProperties;

    @ProfilerConfigurationPropertiesWrapper(prefix = DYNAMIC_PREFIX)
    private DynamicProfilingConfiguration(
            @ProfilerConfigurationProperty(name = MIN_EXECUTION_THRESHOLD, defaultValue = "0", parser = NanoTimePropertyParser.class) final long minExecutionThresholdNs,
            @ProfilerConfigurationProperty(name = EXCLUDED_RESOURCES) final String excludedResources,
            @ProfilerConfigurationProperty(name = EXCLUDED_RESOURCES_MASK) final String excludedResourcesMask,
            @ProfilerConfigurationProperty(name = EXCLUDED_THREADS_MASK) final String excludedThreadsMask,
            @ProfilerConfigurationProperty(name = PROFILING_ROOTS) final String profilingRoots,
            @ProfilerConfigurationProperty(name = PROFILING_ROOTS_MASK) final String profilingRootsMask,
            @ProfilerConfigurationProperty(name = PROFILING_DISABLED) final boolean profilingDisabled,
            @ProfilerConfigurationProperty(name = PROFILED_STACKTRACE_MAX_DEPTH, defaultValue = "-1") final int profiledTraceMaxDepth,
            @ProfilerConfigurationPropertiesWrapper(prefix = SPY_PREFIX, parser = MapConfigurationPropertiesParser.class) final Map<String, String> spiesProperties
    ) {
        super(
                composeResourcesFilter(
                        null,
                        null,
                        excludedResources,
                        excludedResourcesMask,
                        '/'
                ),
                minExecutionThresholdNs
        );
        this.threadsFilter =
                excludedThreadsMask == null || excludedThreadsMask.isEmpty()
                        ? null
                        : Pattern.compile(excludedThreadsMask).asPredicate().negate();
        this.profilingDisabled = profilingDisabled;
        this.profilingRootsFilter = composeResourcesFilter(
                profilingRoots,
                profilingRootsMask,
                null,
                null,
                '.'
        );
        this.profiledTraceMaxDepth = profiledTraceMaxDepth == -1 ? Integer.MAX_VALUE : profiledTraceMaxDepth;
        this.spiesProperties = Collections.unmodifiableMap(spiesProperties);
    }

    public boolean profilingDisabled() {
        return profilingDisabled;
    }

    public Predicate<String> threadsFilter() {
        return threadsFilter;
    }

    public Predicate<String> profilingRootsFilter() {
        return profilingRootsFilter;
    }

    public int profiledTraceMaxDepth() {
        return profiledTraceMaxDepth;
    }

    public Map<String, String> spiesProperties() {
        return spiesProperties;
    }

    @Override
    public String toString() {
        return "DynamicProfilingConfiguration{"
                + "profilingDisabled=" + profilingDisabled
                + ", threadsFilter=" + threadsFilter
                + ", profilingRootsFilter=" + profilingRootsFilter
                + ", profiledTraceMaxDepth=" + profiledTraceMaxDepth
                + ", resourcesFilter=" + resourcesFilter
                + ", minExecutionThresholdNs=" + minExecutionThresholdNs
                + ", spiesProperties=" + spiesProperties
                + '}';
    }
}
