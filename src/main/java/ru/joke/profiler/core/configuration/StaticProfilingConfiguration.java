package ru.joke.profiler.core.configuration;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class StaticProfilingConfiguration extends ProfilingConfiguration {

    private static final String MIN_EXECUTION_THRESHOLD_ARG = "min_execution_threshold";
    private static final String MIN_EXECUTION_THRESHOLD_TU_ARG = "min_execution_threshold_tu";
    private static final String INCLUDED_RESOURCES_ARG = "included_resources";
    private static final String INCLUDED_RESOURCES_MASK_ARG = "included_resources_mask";
    private static final String EXCLUDED_RESOURCES_ARG = "excluded_resources";
    private static final String EXCLUDED_RESOURCES_MASK_ARG = "excluded_resources_mask";

    private StaticProfilingConfiguration(final Predicate<String> resourcesFilter, final long minExecutionThreshold) {
        super(resourcesFilter, minExecutionThreshold);
    }

    @Override
    public String toString() {
        return "StaticProfilingConfiguration{minExecutionThreshold=" + minExecutionThreshold + '}';
    }

    public static StaticProfilingConfiguration parse(final String argsString) {
        if (argsString == null || argsString.isEmpty()) {
            return new StaticProfilingConfiguration(null, 0);
        }

        final Map<String, String> args =
                Arrays.stream(argsString.split(";"))
                        .map(arg -> arg.split("="))
                        .collect(Collectors.toMap(arg -> arg[0], arg -> arg[1]));

        final String includedResourcesStr = args.getOrDefault(INCLUDED_RESOURCES_ARG, "");
        final Set<String> includedResources = parseResourcesArg(includedResourcesStr);

        final String excludedResourcesStr = args.getOrDefault(EXCLUDED_RESOURCES_ARG, "");
        final Set<String> excludedResources = parseResourcesArg(excludedResourcesStr);

        final String includedResourcesMask = args.get(INCLUDED_RESOURCES_MASK_ARG);
        final String excludedResourcesMask = args.get(EXCLUDED_RESOURCES_MASK_ARG);

        final String minExecutionThresholdStr = args.get(MIN_EXECUTION_THRESHOLD_ARG);
        final String minExecThresholdTimeUnitStr = args.get(MIN_EXECUTION_THRESHOLD_TU_ARG);

        final Predicate<String> resourcesFilter = composeResourcesFilter(includedResources, includedResourcesMask, excludedResources, excludedResourcesMask);
        final long executionThresholdNs = parseExecutionThreshold(minExecutionThresholdStr, minExecThresholdTimeUnitStr);

        return new StaticProfilingConfiguration(resourcesFilter, executionThresholdNs);
    }

    private static Predicate<String> composeResourcesFilter(
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

    private static long parseExecutionThreshold(final String thresholdArg, final String thresholdTimeUnitArg) {
        final long minExecutionThreshold = thresholdArg == null || thresholdArg.isEmpty() ? 0 : Long.parseLong(thresholdArg);
        final TimeUnit minExecThresholdTimeUnit = ProfilingTimeUnit.parse(thresholdTimeUnitArg, ProfilingTimeUnit.NANOSECONDS).toJavaTimeUnit();

        return minExecThresholdTimeUnit.toNanos(minExecutionThreshold);
    }

    private static Set<String> parseResourcesArg(final String arg) {
        return Arrays.stream(arg.split(","))
                        .filter(p -> !p.isEmpty())
                        .map(p -> p.replace(".", "/"))
                        .collect(Collectors.toSet());
    }
}
