package ru.joke.profiler.configuration;

import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static ru.joke.profiler.configuration.ConfigurationProperties.*;

public final class DynamicProfilingConfiguration extends ProfilingConfiguration {

    private final boolean profilingDisabled;
    private final Predicate<String> threadsFilter;
    private final Predicate<String> profilingRootsFilter;
    private final int profiledTraceMaxDepth;

    private DynamicProfilingConfiguration(
            final long minExecutionThresholdNs,
            final Predicate<String> resourcesFilter,
            final Predicate<String> threadsFilter,
            final Predicate<String> profilingRootsFilter,
            final boolean profilingDisabled,
            final int profiledTraceMaxDepth) {
        super(resourcesFilter, minExecutionThresholdNs);
        this.threadsFilter = threadsFilter;
        this.profilingDisabled = profilingDisabled;
        this.profilingRootsFilter = profilingRootsFilter;
        this.profiledTraceMaxDepth = profiledTraceMaxDepth;
    }

    public boolean isProfilingDisabled() {
        return profilingDisabled;
    }

    public Predicate<String> getThreadsFilter() {
        return threadsFilter;
    }

    public Predicate<String> getProfilingRootsFilter() {
        return profilingRootsFilter;
    }

    public int getProfiledTraceMaxDepth() {
        return profiledTraceMaxDepth;
    }

    @Override
    public String toString() {
        return "DynamicProfilingConfiguration{"
                + "profilingDisabled=" + profilingDisabled
                + ", threadsFilter=" + threadsFilter
                + ", profilingRootsFilter=" + profilingRootsFilter
                + ", profiledTraceMaxDepth=" + profiledTraceMaxDepth
                + ", resourcesFilter=" + resourcesFilter
                + ", minExecutionThreshold=" + minExecutionThreshold
                + '}';
    }

    static DynamicProfilingConfiguration create(final Properties properties) {
        final String minExecutionThresholdStr = properties.getProperty(DYNAMIC_MIN_EXECUTION_THRESHOLD);
        final String minExecutionThresholdTimeUnitStr = properties.getProperty(DYNAMIC_MIN_EXECUTION_THRESHOLD_TU);
        final long minExecutionThresholdNs = parseExecutionThreshold(minExecutionThresholdStr, minExecutionThresholdTimeUnitStr);

        final String excludedResourcesArg = properties.getProperty(DYNAMIC_EXCLUDED_RESOURCES, "");
        final Set<String> excludedResources = parseResourcesArg(excludedResourcesArg, '.');

        final String excludedResourcesMask = properties.getProperty(DYNAMIC_EXCLUDED_RESOURCES_MASK);
        final String excludedThreadsMask = properties.getProperty(DYNAMIC_EXCLUDED_THREADS_MASK);
        final Predicate<String> threadsFilter =
                excludedThreadsMask == null || excludedThreadsMask.isEmpty()
                        ? null
                        : Pattern.compile(excludedThreadsMask).asPredicate().negate();
        final Predicate<String> resourcesFilter = composeResourcesFilter(excludedResources, excludedResourcesMask, true);

        final String profilingRootsStr = properties.getProperty(DYNAMIC_PROFILING_ROOTS, "");
        final Set<String> profilingRoots = parseResourcesArg(profilingRootsStr, '.');

        final String profilingRootsMask = properties.getProperty(DYNAMIC_PROFILING_ROOTS_MASK);
        final Predicate<String> profilingRootsFilter = composeResourcesFilter(profilingRoots, profilingRootsMask, false);

        final int traceMaxDepth = parseIntProperty(properties, DYNAMIC_PROFILED_STACKTRACE_MAX_DEPTH, Integer.MAX_VALUE);
        final boolean profilingDisabled = parseBooleanProperty(properties, DYNAMIC_PROFILING_DISABLED);
        return new DynamicProfilingConfiguration(
                minExecutionThresholdNs,
                resourcesFilter,
                threadsFilter,
                profilingRootsFilter,
                profilingDisabled,
                traceMaxDepth
        );
    }
}
