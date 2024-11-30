package ru.joke.profiler.core.configuration;

import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static ru.joke.profiler.core.configuration.ConfigurationProperties.*;

public final class DynamicProfilingConfiguration extends ProfilingConfiguration {

    private final boolean profilingDisabled;
    private final Predicate<String> threadsFilter;

    private DynamicProfilingConfiguration(
            final long minExecutionThresholdNs,
            final Predicate<String> resourcesFilter,
            final Predicate<String> threadsFilter,
            final boolean profilingDisabled) {
        super(resourcesFilter, minExecutionThresholdNs);
        this.threadsFilter = threadsFilter;
        this.profilingDisabled = profilingDisabled;
    }

    public boolean isProfilingDisabled() {
        return profilingDisabled;
    }

    public Predicate<String> getThreadsFilter() {
        return threadsFilter;
    }

    @Override
    public String toString() {
        return "DynamicProfilingConfiguration{" + "profilingDisabled=" + profilingDisabled + ", minExecutionThreshold=" + minExecutionThreshold + '}';
    }

    static DynamicProfilingConfiguration create(final Properties properties) {
        final String minExecutionThresholdStr = properties.getProperty(DYNAMIC_MIN_EXECUTION_THRESHOLD);
        final String minExecutionThresholdTimeUnitStr = properties.getProperty(DYNAMIC_MIN_EXECUTION_THRESHOLD_TU);
        final long minExecutionThresholdNs = parseExecutionThreshold(minExecutionThresholdStr, minExecutionThresholdTimeUnitStr);

        final String excludedResourcesArg = properties.getProperty(DYNAMIC_EXCLUDED_RESOURCES, "");
        final Set<String> excludedResources = parseResourcesArg(excludedResourcesArg, '.');

        final String excludedResourcesMask = properties.getProperty(DYNAMIC_EXCLUDED_RESOURCES_MASK);
        final String excludedThreadsMask = properties.getProperty(DYNAMIC_EXCLUDED_THREADS_MASK);
        final String profilingDisabledStr = properties.getProperty(DYNAMIC_PROFILING_DISABLED);
        final Predicate<String> threadsFilter =
                excludedThreadsMask == null || excludedThreadsMask.isEmpty()
                        ? null
                        : Pattern.compile(excludedThreadsMask).asPredicate().negate();
        final Predicate<String> resourcesFilter = composeResourcesFilter(excludedResources, excludedResourcesMask, true);

        return new DynamicProfilingConfiguration(
                minExecutionThresholdNs,
                resourcesFilter,
                threadsFilter,
                Boolean.parseBoolean(profilingDisabledStr)
        );
    }
}
