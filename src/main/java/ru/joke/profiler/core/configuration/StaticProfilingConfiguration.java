package ru.joke.profiler.core.configuration;

import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

import static ru.joke.profiler.core.configuration.ConfigurationProperties.*;

public final class StaticProfilingConfiguration extends ProfilingConfiguration {

    private static final long DEFAULT_DYNAMIC_CONFIG_REFRESHING_INTERVAL = 60_000;

    private final boolean dynamicConfigurationEnabled;
    private final long dynamicConfigurationRefreshInterval;
    private final boolean executionTracingEnabled;

    StaticProfilingConfiguration(
            final Predicate<String> resourcesFilter,
            final long minExecutionThreshold,
            final boolean dynamicConfigurationEnabled,
            final long dynamicConfigurationRefreshIntervalMs,
            final boolean executionTracingEnabled) {
        super(resourcesFilter, minExecutionThreshold);
        this.dynamicConfigurationEnabled = dynamicConfigurationEnabled;
        this.dynamicConfigurationRefreshInterval = dynamicConfigurationRefreshIntervalMs;
        this.executionTracingEnabled = executionTracingEnabled;
    }

    public boolean isDynamicConfigurationEnabled() {
        return dynamicConfigurationEnabled;
    }

    public long getDynamicConfigurationRefreshInterval() {
        return dynamicConfigurationRefreshInterval;
    }

    public boolean isExecutionTracingEnabled() {
        return executionTracingEnabled;
    }

    @Override
    public String toString() {
        return "StaticProfilingConfiguration{"
                + "dynamicConfigurationEnabled=" + dynamicConfigurationEnabled
                + ", dynamicConfigurationRefreshInterval=" + dynamicConfigurationRefreshInterval
                + ", executionTracingEnabled=" + executionTracingEnabled
                + ", minExecutionThreshold=" + minExecutionThreshold
                + '}';
    }

    static StaticProfilingConfiguration create(final Properties props) {
        final String includedResourcesStr = props.getProperty(STATIC_INCLUDED_RESOURCES, "");
        final Set<String> includedResources = parseResourcesArg(includedResourcesStr, '/');

        final String excludedResourcesStr = props.getProperty(STATIC_EXCLUDED_RESOURCES, "");
        final Set<String> excludedResources = parseResourcesArg(excludedResourcesStr, '/');

        final String includedResourcesMask = props.getProperty(STATIC_INCLUDED_RESOURCES_MASK);
        final String excludedResourcesMask = props.getProperty(STATIC_EXCLUDED_RESOURCES_MASK);

        final String minExecutionThresholdStr = props.getProperty(STATIC_MIN_EXECUTION_THRESHOLD);
        final String minExecThresholdTimeUnitStr = props.getProperty(STATIC_MIN_EXECUTION_THRESHOLD_TU);

        final Predicate<String> resourcesFilter = composeJoinedResourcesFilter(includedResources, includedResourcesMask, excludedResources, excludedResourcesMask);
        final long executionThresholdNs = parseExecutionThreshold(minExecutionThresholdStr, minExecThresholdTimeUnitStr);

        final boolean dynamicConfigurationEnabled = Boolean.parseBoolean(props.getProperty(STATIC_DYNAMIC_CONFIGURATION_ENABLED));
        
        final String dynamicConfigurationRefreshIntervalStr = props.getProperty(STATIC_DYNAMIC_CONFIGURATION_REFRESH_INTERVAL);
        final long dynamicConfigurationRefreshInterval =
                dynamicConfigurationRefreshIntervalStr == null || dynamicConfigurationRefreshIntervalStr.isEmpty()
                        ? DEFAULT_DYNAMIC_CONFIG_REFRESHING_INTERVAL
                        : Long.parseLong(dynamicConfigurationRefreshIntervalStr);

        final String dynamicConfigurationRefreshIntervalTimeUnitStr = props.getProperty(STATIC_DYNAMIC_CONFIGURATION_REFRESH_INTERVAL_TU);
        final ProfilingTimeUnit dynamicConfigurationRefreshIntervalTimeUnit = ProfilingTimeUnit.parse(dynamicConfigurationRefreshIntervalTimeUnitStr, ProfilingTimeUnit.MILLISECONDS);
        final long dynamicConfigurationRefreshIntervalMs = dynamicConfigurationRefreshIntervalTimeUnit.toJavaTimeUnit().toMillis(dynamicConfigurationRefreshInterval);

        final boolean executionTracingEnabled = Boolean.parseBoolean(props.getProperty(STATIC_EXECUTION_TRACING_ENABLED, ""));

        return new StaticProfilingConfiguration(
                resourcesFilter,
                executionThresholdNs,
                dynamicConfigurationEnabled,
                dynamicConfigurationRefreshIntervalMs,
                executionTracingEnabled
        );
    }

    private static Predicate<String> composeJoinedResourcesFilter(
            final Set<String> includedResources,
            final String includedResourcesMask,
            final Set<String> excludedResources,
            final String excludedResourcesMask) {

        final Predicate<String> resourcesFilterByExcluded = composeResourcesFilter(excludedResources, excludedResourcesMask, true);
        final Predicate<String> resourcesFilterByIncluded = composeResourcesFilter(includedResources, includedResourcesMask, false);

        return resourcesFilterByExcluded == null
                ? resourcesFilterByIncluded
                : resourcesFilterByIncluded == null
                    ? resourcesFilterByExcluded
                    : resourcesFilterByIncluded.and(resourcesFilterByExcluded);
    }
}
