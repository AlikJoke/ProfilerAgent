package ru.joke.profiler.core.configuration;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ru.joke.profiler.core.configuration.ConfigurationProperties.*;

public final class StaticProfilingConfiguration extends ProfilingConfiguration {

    private static final long DEFAULT_DYNAMIC_CONFIG_REFRESHING_INTERVAL = 60_000;

    private final boolean dynamicConfigurationEnabled;
    private final String dynamicConfigurationFilePath;
    private final long dynamicConfigurationRefreshInterval;

    private StaticProfilingConfiguration(
            final Predicate<String> resourcesFilter,
            final long minExecutionThreshold,
            final boolean dynamicConfigurationEnabled,
            final String dynamicConfigurationFilePath,
            final long dynamicConfigurationRefreshInterval) {
        super(resourcesFilter, minExecutionThreshold);
        this.dynamicConfigurationEnabled = dynamicConfigurationEnabled;
        this.dynamicConfigurationFilePath = dynamicConfigurationFilePath;
        this.dynamicConfigurationRefreshInterval = dynamicConfigurationRefreshInterval;
    }

    public boolean isDynamicConfigurationEnabled() {
        return dynamicConfigurationEnabled;
    }

    public String getDynamicConfigurationFilePath() {
        return dynamicConfigurationFilePath;
    }

    public long getDynamicConfigurationRefreshInterval() {
        return dynamicConfigurationRefreshInterval;
    }

    @Override
    public String toString() {
        return "StaticProfilingConfiguration{" + "dynamicConfigurationEnabled=" + dynamicConfigurationEnabled + ", dynamicConfigurationFilePath='" + dynamicConfigurationFilePath + '\'' + ", dynamicConfigurationRefreshInterval=" + dynamicConfigurationRefreshInterval + ", minExecutionThreshold=" + minExecutionThreshold + '}';
    }

    public static StaticProfilingConfiguration parse(final String argsString) {
        if (argsString == null || argsString.isEmpty()) {
            return new StaticProfilingConfiguration(
                    null,
                    0,
                    false,
                    null,
                    0
            );
        }

        final Map<String, String> args =
                Arrays.stream(argsString.split(";"))
                        .map(arg -> arg.split("="))
                        .collect(Collectors.toMap(arg -> arg[0], arg -> arg[1]));

        final String includedResourcesStr = args.getOrDefault(INCLUDED_RESOURCES, "");
        final Set<String> includedResources = parseResourcesArg(includedResourcesStr, '/');

        final String excludedResourcesStr = args.getOrDefault(EXCLUDED_RESOURCES, "");
        final Set<String> excludedResources = parseResourcesArg(excludedResourcesStr, '/');

        final String includedResourcesMask = args.get(INCLUDED_RESOURCES_MASK);
        final String excludedResourcesMask = args.get(EXCLUDED_RESOURCES_MASK);

        final String minExecutionThresholdStr = args.get(MIN_EXECUTION_THRESHOLD);
        final String minExecThresholdTimeUnitStr = args.get(MIN_EXECUTION_THRESHOLD_TU);

        final Predicate<String> resourcesFilter = composeJoinedResourcesFilter(includedResources, includedResourcesMask, excludedResources, excludedResourcesMask);
        final long executionThresholdNs = parseExecutionThreshold(minExecutionThresholdStr, minExecThresholdTimeUnitStr);

        final boolean dynamicConfigurationEnabled = Boolean.parseBoolean(args.get(DYNAMIC_CONFIGURATION_ENABLED));
        final String dynamicConfigurationFilePath = args.get(DYNAMIC_CONFIGURATION_FILEPATH);

        final String dynamicConfigurationRefreshIntervalStr = args.get(DYNAMIC_CONFIGURATION_REFRESH_INTERVAL);
        final long dynamicConfigurationRefreshInterval =
                dynamicConfigurationRefreshIntervalStr == null || dynamicConfigurationRefreshIntervalStr.isEmpty()
                        ? DEFAULT_DYNAMIC_CONFIG_REFRESHING_INTERVAL
                        : Long.parseLong(dynamicConfigurationRefreshIntervalStr);

        final String dynamicConfigurationRefreshIntervalTimeUnitStr = args.get(DYNAMIC_CONFIGURATION_REFRESH_INTERVAL_TU);
        final ProfilingTimeUnit dynamicConfigurationRefreshIntervalTimeUnit = ProfilingTimeUnit.parse(dynamicConfigurationRefreshIntervalTimeUnitStr, ProfilingTimeUnit.MILLISECONDS);
        final long dynamicConfigurationRefreshIntervalMs = dynamicConfigurationRefreshIntervalTimeUnit.toJavaTimeUnit().toMillis(dynamicConfigurationRefreshInterval);

        return new StaticProfilingConfiguration(
                resourcesFilter,
                executionThresholdNs,
                dynamicConfigurationEnabled,
                dynamicConfigurationFilePath,
                dynamicConfigurationRefreshIntervalMs
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
