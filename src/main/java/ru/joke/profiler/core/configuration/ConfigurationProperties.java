package ru.joke.profiler.core.configuration;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class ConfigurationProperties {

    private static final String STATIC_PREFIX = "static.";
    private static final String DYNAMIC_PREFIX = "dynamic.";
    
    private static final String ABS_MIN_EXECUTION_THRESHOLD = "min_execution_threshold";
    private static final String ABS_MIN_EXECUTION_THRESHOLD_TU = "min_execution_threshold_tu";
    private static final String ABS_EXCLUDED_RESOURCES = "excluded_resources";
    private static final String ABS_EXCLUDED_RESOURCES_MASK = "excluded_resources_mask";

    static final String SINK_PROPERTIES_PREFIX = STATIC_PREFIX + "sink.";
    static final String STATIC_SINK_TYPE = createProperty(SINK_PROPERTIES_PREFIX, "type");

    private static final String OUTPUT_DATA_FORMAT = "output_data_format";
    private static final String OUTPUT_STREAM_BUFFER_SIZE = "output_stream_buffer_size";
    private static final String OUTPUT_STREAM_FORCE_FLUSH_ON_WRITES = "output_stream_force_flush_on_writes";

    public static final String ASYNC_FLUSHING_ENABLED = createProperty(SINK_PROPERTIES_PREFIX, "async_flushing_enabled");
    public static final String ASYNC_FLUSHING_INTERVAL = createProperty(SINK_PROPERTIES_PREFIX, "async_flushing_interval_ms");
    public static final String ASYNC_FLUSHING_POOL_SIZE = createProperty(SINK_PROPERTIES_PREFIX, "async_flushing_pool_size");
    public static final String ASYNC_FLUSHING_QUEUE_OVERFLOW_LIMIT = createProperty(SINK_PROPERTIES_PREFIX, "async_flushing_queue_overflow_limit");
    public static final String ASYNC_FLUSHING_QUEUE_OVERFLOW_POLICY = createProperty(SINK_PROPERTIES_PREFIX, "async_flushing_queue_overflow_policy");
    public static final String ASYNC_FLUSHING_FORCE_ON_EXIT = createProperty(SINK_PROPERTIES_PREFIX, "async_flushing_force_on_exit");
    public static final String ASYNC_FLUSHING_MAX_BATCH_SIZE = createProperty(SINK_PROPERTIES_PREFIX, "async_flushing_max_batch_size");

    public static final String CONSOLE_SINK_PROPERTIES_PREFIX = createProperty(SINK_PROPERTIES_PREFIX, "console.");

    public static final String STATIC_CONSOLE_SINK_DATA_FORMAT = createProperty(CONSOLE_SINK_PROPERTIES_PREFIX, OUTPUT_DATA_FORMAT);
    public static final String STATIC_CONSOLE_SINK_BUFFER_SIZE = createProperty(CONSOLE_SINK_PROPERTIES_PREFIX, OUTPUT_STREAM_BUFFER_SIZE);
    public static final String STATIC_CONSOLE_SINK_FORCE_FLUSH_ON_WRITES = createProperty(CONSOLE_SINK_PROPERTIES_PREFIX, OUTPUT_STREAM_FORCE_FLUSH_ON_WRITES);

    public static final String FILE_SINK_PROPERTIES_PREFIX = createProperty(SINK_PROPERTIES_PREFIX, "file.");

    public static final String STATIC_FILE_SINK_DATA_FORMAT = createProperty(FILE_SINK_PROPERTIES_PREFIX, OUTPUT_DATA_FORMAT);
    public static final String STATIC_FILE_SINK_BUFFER_SIZE = createProperty(FILE_SINK_PROPERTIES_PREFIX, OUTPUT_STREAM_BUFFER_SIZE);
    public static final String STATIC_FILE_SINK_FILE = createProperty(FILE_SINK_PROPERTIES_PREFIX, "output_file");
    public static final String STATIC_FILE_SINK_EXISTING_FILE_POLICY = createProperty(FILE_SINK_PROPERTIES_PREFIX, "existing_output_file_policy");
    public static final String STATIC_FILE_SINK_FORCE_FLUSH_ON_WRITES = createProperty(FILE_SINK_PROPERTIES_PREFIX, OUTPUT_STREAM_FORCE_FLUSH_ON_WRITES);

    public static final String LOGGER_SINK_PROPERTIES_PREFIX = createProperty(SINK_PROPERTIES_PREFIX, "logger.");

    public static final String STATIC_LOGGER_SINK_DATA_FORMAT = createProperty(LOGGER_SINK_PROPERTIES_PREFIX, OUTPUT_DATA_FORMAT);
    public static final String STATIC_LOGGER_SINK_CATEGORY = createProperty(LOGGER_SINK_PROPERTIES_PREFIX, "category");
    public static final String STATIC_LOGGER_SINK_LEVEL = createProperty(LOGGER_SINK_PROPERTIES_PREFIX, "level");

    public static final String JDBC_SINK_PROPERTIES_PREFIX = createProperty(SINK_PROPERTIES_PREFIX, "jdbc.");

    public static final String JDBC_SINK_INSERTION_PROPERTIES_PREFIX = createProperty(JDBC_SINK_PROPERTIES_PREFIX, "insertion.");

    public static final String STATIC_JDBC_SINK_ENABLE_BATCHING = createProperty(JDBC_SINK_INSERTION_PROPERTIES_PREFIX, "enable_batching");
    public static final String STATIC_JDBC_SINK_BATCH_SIZE = createProperty(JDBC_SINK_INSERTION_PROPERTIES_PREFIX, "batch_size");

    public static final String JDBC_SINK_CONNECTION_FACTORY_PROPERTIES_PREFIX = createProperty(JDBC_SINK_PROPERTIES_PREFIX, "connection-factory.");
    public static final String STATIC_JDBC_SINK_CONNECTION_FACTORY_URL = createProperty(JDBC_SINK_CONNECTION_FACTORY_PROPERTIES_PREFIX, "url");

    public static final String JDBC_SINK_CONNECTION_POOL_PROPERTIES_PREFIX = createProperty(JDBC_SINK_PROPERTIES_PREFIX, "connection-pool.");
    public static final String STATIC_JDBC_SINK_CONNECTION_POOL_ENABLED = createProperty(JDBC_SINK_CONNECTION_POOL_PROPERTIES_PREFIX, "use_connection_pool");
    public static final String STATIC_JDBC_SINK_CONNECTION_POOL_MAX_POOL = createProperty(JDBC_SINK_CONNECTION_POOL_PROPERTIES_PREFIX, "max_pool_size");
    public static final String STATIC_JDBC_SINK_CONNECTION_POOL_INIT_POOL = createProperty(JDBC_SINK_CONNECTION_POOL_PROPERTIES_PREFIX, "initial_pool_size");
    public static final String STATIC_JDBC_SINK_CONNECTION_POOL_KEEP_ALIVE_IDLE = createProperty(JDBC_SINK_CONNECTION_POOL_PROPERTIES_PREFIX, "keep_alive_idle_time");
    public static final String STATIC_JDBC_SINK_CONNECTION_POOL_MAX_WAIT = createProperty(JDBC_SINK_CONNECTION_POOL_PROPERTIES_PREFIX, "max_connection_wait_time");

    public static final String JDBC_SINK_OUTPUT_TABLE_PROPERTIES_PREFIX = createProperty(JDBC_SINK_PROPERTIES_PREFIX, "output-table.");
    public static final String STATIC_JDBC_SINK_OUTPUT_TABLE_NAME = createProperty(JDBC_SINK_OUTPUT_TABLE_PROPERTIES_PREFIX, "table_name");
    public static final String STATIC_JDBC_SINK_EXISTING_TABLE_POLICY = createProperty(JDBC_SINK_OUTPUT_TABLE_PROPERTIES_PREFIX, "existing_table_policy");
    public static final String STATIC_JDBC_SINK_AUTO_CREATE_OUTPUT_TABLE = createProperty(JDBC_SINK_OUTPUT_TABLE_PROPERTIES_PREFIX, "auto_create_table");
    public static final String STATIC_JDBC_SINK_SKIP_SCHEMA_VALIDATION = createProperty(JDBC_SINK_OUTPUT_TABLE_PROPERTIES_PREFIX, "skip_schema_validation");
    public static final String STATIC_JDBC_SINK_COLUMNS_METADATA = createProperty(JDBC_SINK_OUTPUT_TABLE_PROPERTIES_PREFIX, "columns_metadata");

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
    static final String DYNAMIC_PROFILING_ROOTS = createDynamicProperty("profiling_roots");
    static final String DYNAMIC_PROFILING_ROOTS_MASK = createDynamicProperty("profiling_roots_mask");
    static final String DYNAMIC_PROFILED_STACKTRACE_MAX_DEPTH = createDynamicProperty("profiled_stacktrace_max_depth");

    static final String CONFIGURATION_FILE_PATH_ARG = "conf_file";

    public static String findRequiredProperty(final Map<?, ?> properties, final String propertyName) {
        final Object propertyValue = properties.get(propertyName);
        final String propertyValueStr = propertyValue == null ? null : propertyValue.toString();
        if (propertyValueStr == null || propertyValueStr.isEmpty()) {
            throw new InvalidConfigurationException(String.format("Property (%s) is required", propertyName));
        }

        return propertyValueStr;
    }

    public static int parseIntProperty(
            final Map<?, ?> properties,
            final String property,
            final int defaultValue) {
        final Object propertyValue = properties.get(property);
        final String propertyValueStr = propertyValue == null ? null : propertyValue.toString();
        return propertyValueStr == null || propertyValueStr.isEmpty()
                ? defaultValue
                : Integer.parseInt(propertyValueStr);
    }

    public static long parseLongProperty(
            final Map<?, ?> properties,
            final String property,
            final long defaultValue) {
        final Object propertyValue = properties.get(property);
        final String propertyValueStr = propertyValue == null ? null : propertyValue.toString();
        return propertyValueStr == null || propertyValueStr.isEmpty()
                ? defaultValue
                : Long.parseLong(propertyValueStr);
    }

    public static boolean parseBooleanProperty(final Map<?, ?> properties, final String property) {
        final Object propertyValue = properties.get(property);
        final String propertyValueStr = propertyValue == null ? null : propertyValue.toString();
        return Boolean.parseBoolean(propertyValueStr);
    }

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
