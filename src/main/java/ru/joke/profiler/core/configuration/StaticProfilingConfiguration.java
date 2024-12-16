package ru.joke.profiler.core.configuration;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ru.joke.profiler.core.configuration.ConfigurationProperties.*;

public final class StaticProfilingConfiguration extends ProfilingConfiguration {

    private static final long DEFAULT_DYNAMIC_CONFIG_REFRESHING_INTERVAL = 60_000;

    private final boolean dynamicConfigurationEnabled;
    private final long dynamicConfigurationRefreshInterval;
    private final boolean executionTracingEnabled;
    private final String sinkType;
    private final Map<String, String> sinkProperties;

    StaticProfilingConfiguration(
            final Predicate<String> resourcesFilter,
            final long minExecutionThreshold,
            final boolean dynamicConfigurationEnabled,
            final long dynamicConfigurationRefreshIntervalMs,
            final boolean executionTracingEnabled,
            final String sinkType,
            final Map<String, String> sinkProperties) {
        super(resourcesFilter, minExecutionThreshold);
        this.dynamicConfigurationEnabled = dynamicConfigurationEnabled;
        this.dynamicConfigurationRefreshInterval = dynamicConfigurationRefreshIntervalMs;
        this.executionTracingEnabled = executionTracingEnabled;
        this.sinkType = sinkType;
        this.sinkProperties = Collections.unmodifiableMap(sinkProperties);
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

    public Map<String, String> getSinkProperties() {
        return sinkProperties;
    }

    public String getSinkType() {
        return sinkType;
    }

    @Override
    public String toString() {
        return "StaticProfilingConfiguration{"
                + "dynamicConfigurationEnabled=" + dynamicConfigurationEnabled
                + ", dynamicConfigurationRefreshInterval=" + dynamicConfigurationRefreshInterval
                + ", executionTracingEnabled=" + executionTracingEnabled
                + ", minExecutionThreshold=" + minExecutionThreshold
                + ", sinkType=" + sinkType
                + ", sinkProperties=" + sinkProperties
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

        final boolean dynamicConfigurationEnabled = parseBooleanProperty(props, STATIC_DYNAMIC_CONFIGURATION_ENABLED);

        final long dynamicConfigurationRefreshInterval = parseLongProperty(props, STATIC_DYNAMIC_CONFIGURATION_REFRESH_INTERVAL, DEFAULT_DYNAMIC_CONFIG_REFRESHING_INTERVAL);

        final String dynamicConfigurationRefreshIntervalTimeUnitStr = props.getProperty(STATIC_DYNAMIC_CONFIGURATION_REFRESH_INTERVAL_TU);
        final ProfilingTimeUnit dynamicConfigurationRefreshIntervalTimeUnit = ProfilingTimeUnit.parse(dynamicConfigurationRefreshIntervalTimeUnitStr, ProfilingTimeUnit.MILLISECONDS);
        final long dynamicConfigurationRefreshIntervalMs = dynamicConfigurationRefreshIntervalTimeUnit.toJavaTimeUnit().toMillis(dynamicConfigurationRefreshInterval);

        final boolean executionTracingEnabled = parseBooleanProperty(props, STATIC_EXECUTION_TRACING_ENABLED);
        final Map<String, String> sinkProperties =
                props.entrySet()
                        .stream()
                        .filter(e -> e.getKey().toString().startsWith(SINK_PROPERTIES_PREFIX))
                        .filter(e -> e.getValue() != null && !String.valueOf(e.getValue()).isEmpty())
                        .collect(Collectors.toMap(e -> e.getKey().toString(), e -> String.valueOf(e.getValue())));

        final String sinkType = props.getProperty(STATIC_SINK_TYPE);

        return new StaticProfilingConfiguration(
                resourcesFilter,
                executionThresholdNs,
                dynamicConfigurationEnabled,
                dynamicConfigurationRefreshIntervalMs,
                executionTracingEnabled,
                sinkType,
                sinkProperties
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
