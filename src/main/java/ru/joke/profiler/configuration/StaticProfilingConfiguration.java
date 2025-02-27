package ru.joke.profiler.configuration;

import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.util.MapConfigurationPropertiesParser;
import ru.joke.profiler.configuration.util.MillisTimePropertyParser;
import ru.joke.profiler.configuration.util.NanoTimePropertyParser;
import ru.joke.profiler.configuration.util.TokenizeCommaDelimitedStringPropertyParser;
import ru.joke.profiler.output.sinks.fs.stream.console.OutputDataConsoleSinkHandle;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;
import static ru.joke.profiler.util.ArgUtil.checkPositive;

public final class StaticProfilingConfiguration extends ProfilingConfiguration {

    private static final String STATIC_PREFIX = "static.";
    private static final String SINK_PROPERTIES_PREFIX = "sink.";
    private static final String ACTIVE_SPIES = "spies";
    private static final String ACTIVE_SINKS = "sinks";
    private static final String IGNORE_SINK_ERRORS = "ignore_errors";
    private static final String SPY_PREFIX = "spy.";
    private static final String INCLUDED_RESOURCES = "included_resources";
    private static final String INCLUDED_RESOURCES_MASK = "included_resources_mask";
    private static final String DYNAMIC_CONFIGURATION_ENABLED = "dynamic_conf_enabled";
    private static final String DYNAMIC_CONFIGURATION_REFRESH_INTERVAL = "dynamic_conf_refresh_interval";

    private final boolean dynamicConfigurationEnabled;
    private final long dynamicConfigurationRefreshIntervalMs;
    private final List<String> spies;
    private final List<String> sinks;
    private final Map<String, String> sinkProperties;
    private final Map<String, String> spiesProperties;
    private final boolean ignoreSinkErrors;

    @ProfilerConfigurationPropertiesWrapper(prefix = STATIC_PREFIX)
    StaticProfilingConfiguration(
            @ProfilerConfigurationProperty(name = INCLUDED_RESOURCES) final String includedResources,
            @ProfilerConfigurationProperty(name = INCLUDED_RESOURCES_MASK) final String includedResourcesMask,
            @ProfilerConfigurationProperty(name = EXCLUDED_RESOURCES) final String excludedResources,
            @ProfilerConfigurationProperty(name = EXCLUDED_RESOURCES_MASK) final String excludedResourcesMask,
            @ProfilerConfigurationProperty(name = MIN_EXECUTION_THRESHOLD, defaultValue = "0", parser = NanoTimePropertyParser.class) final long minExecutionThresholdNs,
            @ProfilerConfigurationProperty(name = DYNAMIC_CONFIGURATION_ENABLED) final boolean dynamicConfigurationEnabled,
            @ProfilerConfigurationProperty(name = DYNAMIC_CONFIGURATION_REFRESH_INTERVAL, defaultValue = "1m", parser = MillisTimePropertyParser.class) final long dynamicConfigurationRefreshIntervalMs,
            @ProfilerConfigurationProperty(name = ACTIVE_SINKS, defaultValue = OutputDataConsoleSinkHandle.SINK_TYPE, parser = TokenizeCommaDelimitedStringPropertyParser.class) final List<String> sinks,
            @ProfilerConfigurationPropertiesWrapper(prefix = SINK_PROPERTIES_PREFIX, parser = MapConfigurationPropertiesParser.class) final Map<String, String> sinkProperties,
            @ProfilerConfigurationProperty(name = ACTIVE_SPIES, parser = TokenizeCommaDelimitedStringPropertyParser.class) final List<String> spies,
            @ProfilerConfigurationPropertiesWrapper(prefix = SPY_PREFIX, parser = MapConfigurationPropertiesParser.class) final Map<String, String> spiesProperties
    ) {
        super(
                composeResourcesFilter(
                        includedResources,
                        includedResourcesMask,
                        excludedResources,
                        excludedResourcesMask,
                        '/'
                ),
                minExecutionThresholdNs
        );
        this.dynamicConfigurationEnabled = dynamicConfigurationEnabled;
        this.dynamicConfigurationRefreshIntervalMs = checkPositive(dynamicConfigurationRefreshIntervalMs, "dynamicConfigurationRefreshIntervalMs");
        this.sinks = Collections.unmodifiableList(checkNotNull(sinks, "sinks"));
        this.sinkProperties = Collections.unmodifiableMap(checkNotNull(sinkProperties, "sinkProperties"));
        this.ignoreSinkErrors = Boolean.parseBoolean(sinkProperties.get(IGNORE_SINK_ERRORS));
        this.spies = Collections.unmodifiableList(checkNotNull(spies, "spies"));
        this.spiesProperties = Collections.unmodifiableMap(checkNotNull(spiesProperties, "spiesProperties"));
    }

    public boolean dynamicConfigurationEnabled() {
        return dynamicConfigurationEnabled;
    }

    public boolean ignoreSinkErrors() {
        return ignoreSinkErrors;
    }

    public long dynamicConfigurationRefreshIntervalMs() {
        return dynamicConfigurationRefreshIntervalMs;
    }

    public Map<String, String> sinkProperties() {
        return sinkProperties;
    }

    public List<String> spies() {
        return spies;
    }

    public List<String> sinks() {
        return sinks;
    }

    public Map<String, String> spiesProperties() {
        return spiesProperties;
    }

    @Override
    public String toString() {
        return "StaticProfilingConfiguration{"
                + "dynamicConfigurationEnabled=" + dynamicConfigurationEnabled
                + ", dynamicConfigurationRefreshIntervalMs=" + dynamicConfigurationRefreshIntervalMs
                + ", minExecutionThreshold=" + minExecutionThresholdNs
                + ", sinks=" + sinks
                + ", sinkProperties=" + sinkProperties
                + ", spies=" + spies
                + ", spiesProperties=" + spiesProperties
                + ", ignoreSinkErrors=" + ignoreSinkErrors
                + '}';
    }
}
