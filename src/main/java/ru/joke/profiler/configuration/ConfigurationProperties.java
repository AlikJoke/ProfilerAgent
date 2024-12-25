package ru.joke.profiler.configuration;

import ru.joke.profiler.output.handlers.http2.OutputDataHttp2SinkHandle;
import ru.joke.profiler.output.handlers.jdbc.OutputDataJdbcSinkHandle;
import ru.joke.profiler.output.handlers.jul.OutputDataLoggerSinkHandle;
import ru.joke.profiler.output.handlers.kafka.OutputDataKafkaSinkHandle;
import ru.joke.profiler.output.handlers.stream.console.OutputDataConsoleSinkHandle;
import ru.joke.profiler.output.handlers.stream.file.OutputDataFileSinkHandle;

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

    public static final String CONSOLE_SINK_PROPERTIES_PREFIX = createProperty(SINK_PROPERTIES_PREFIX, createProperty(OutputDataConsoleSinkHandle.SINK_TYPE, "."));

    public static final String STATIC_CONSOLE_SINK_DATA_FORMAT = createProperty(CONSOLE_SINK_PROPERTIES_PREFIX, OUTPUT_DATA_FORMAT);
    public static final String STATIC_CONSOLE_SINK_BUFFER_SIZE = createProperty(CONSOLE_SINK_PROPERTIES_PREFIX, OUTPUT_STREAM_BUFFER_SIZE);
    public static final String STATIC_CONSOLE_SINK_FORCE_FLUSH_ON_WRITES = createProperty(CONSOLE_SINK_PROPERTIES_PREFIX, OUTPUT_STREAM_FORCE_FLUSH_ON_WRITES);

    public static final String FILE_SINK_PROPERTIES_PREFIX = createProperty(SINK_PROPERTIES_PREFIX, createProperty(OutputDataFileSinkHandle.SINK_TYPE, "."));

    public static final String STATIC_FILE_SINK_DATA_FORMAT = createProperty(FILE_SINK_PROPERTIES_PREFIX, OUTPUT_DATA_FORMAT);
    public static final String STATIC_FILE_SINK_BUFFER_SIZE = createProperty(FILE_SINK_PROPERTIES_PREFIX, OUTPUT_STREAM_BUFFER_SIZE);
    public static final String STATIC_FILE_SINK_FILE = createProperty(FILE_SINK_PROPERTIES_PREFIX, "output_file");
    public static final String STATIC_FILE_SINK_EXISTING_FILE_POLICY = createProperty(FILE_SINK_PROPERTIES_PREFIX, "existing_output_file_policy");
    public static final String STATIC_FILE_SINK_FORCE_FLUSH_ON_WRITES = createProperty(FILE_SINK_PROPERTIES_PREFIX, OUTPUT_STREAM_FORCE_FLUSH_ON_WRITES);

    public static final String LOGGER_SINK_PROPERTIES_PREFIX = createProperty(SINK_PROPERTIES_PREFIX, createProperty(OutputDataLoggerSinkHandle.SINK_TYPE, "."));

    public static final String STATIC_LOGGER_SINK_DATA_FORMAT = createProperty(LOGGER_SINK_PROPERTIES_PREFIX, OUTPUT_DATA_FORMAT);
    public static final String STATIC_LOGGER_SINK_CATEGORY = createProperty(LOGGER_SINK_PROPERTIES_PREFIX, "category");
    public static final String STATIC_LOGGER_SINK_LEVEL = createProperty(LOGGER_SINK_PROPERTIES_PREFIX, "level");

    public static final String JDBC_SINK_PROPERTIES_PREFIX = createProperty(SINK_PROPERTIES_PREFIX, createProperty(OutputDataJdbcSinkHandle.SINK_TYPE, "."));

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

    public static final String KAFKA_SINK_PROPERTIES_PREFIX = createProperty(SINK_PROPERTIES_PREFIX, createProperty(OutputDataKafkaSinkHandle.SINK_TYPE, "."));

    public static final String STATIC_KAFKA_SINK_PRODUCER_PROPERTIES_PREFIX = createProperty(KAFKA_SINK_PROPERTIES_PREFIX, "producer.");

    public static final String STATIC_KAFKA_SINK_PRODUCER_DISABLE_CLUSTER_VALIDATION_ON_START = createProperty(STATIC_KAFKA_SINK_PRODUCER_PROPERTIES_PREFIX, "disable_cluster_validation_on_start");
    public static final String STATIC_KAFKA_SINK_PRODUCER_DISABLE_COMPRESSION = createProperty(STATIC_KAFKA_SINK_PRODUCER_PROPERTIES_PREFIX, "disable_compression");
    public static final String STATIC_KAFKA_SINK_PRODUCER_WAIT_ON_CLOSE_MS = createProperty(STATIC_KAFKA_SINK_PRODUCER_PROPERTIES_PREFIX, "wait_on_close_timeout_ms");

    public static final String STATIC_KAFKA_SINK_OUTPUT_MESSAGE_CONFIGURATION_PREFIX = createProperty(KAFKA_SINK_PROPERTIES_PREFIX, "output-message.");

    public static final String STATIC_KAFKA_SINK_MESSAGE_OUTPUT_QUEUE = createProperty(STATIC_KAFKA_SINK_OUTPUT_MESSAGE_CONFIGURATION_PREFIX, "target_queue");
    public static final String STATIC_KAFKA_SINK_MESSAGE_TYPE = createProperty(STATIC_KAFKA_SINK_OUTPUT_MESSAGE_CONFIGURATION_PREFIX, "message_type");
    public static final String STATIC_KAFKA_SINK_MESSAGE_TYPE_HEADER = createProperty(STATIC_KAFKA_SINK_OUTPUT_MESSAGE_CONFIGURATION_PREFIX, "message_type_header");
    public static final String STATIC_KAFKA_SINK_MESSAGE_PROPERTIES_MAPPING = createProperty(STATIC_KAFKA_SINK_OUTPUT_MESSAGE_CONFIGURATION_PREFIX, "properties_mapping");
    public static final String STATIC_KAFKA_SINK_MESSAGE_HEADERS_MAPPING = createProperty(STATIC_KAFKA_SINK_OUTPUT_MESSAGE_CONFIGURATION_PREFIX, "headers_mapping");

    public static final String STATIC_KAFKA_SINK_CONN_RECOVERY_CONFIGURATION_PREFIX = createProperty(KAFKA_SINK_PROPERTIES_PREFIX, "connection-recovery.");

    public static final String STATIC_KAFKA_SINK_RECOVERY_TIMEOUT_MS = createProperty(STATIC_KAFKA_SINK_CONN_RECOVERY_CONFIGURATION_PREFIX, "timeout_ms");
    public static final String STATIC_KAFKA_SINK_RECOVERY_MAX_RETRY_INTERVAL_MS = createProperty(STATIC_KAFKA_SINK_CONN_RECOVERY_CONFIGURATION_PREFIX, "max_retry_interval_ms");
    public static final String STATIC_KAFKA_SINK_RECOVERY_PROCESSING_POLICY = createProperty(STATIC_KAFKA_SINK_CONN_RECOVERY_CONFIGURATION_PREFIX, "processing_policy");

    public static final String HTTP2_SINK_PROPERTIES_PREFIX = createProperty(SINK_PROPERTIES_PREFIX, createProperty(OutputDataHttp2SinkHandle.SINK_TYPE, "."));

    public static final String STATIC_HTTP2_SINK_OUTPUT_MESSAGE_CONFIGURATION_PREFIX = createProperty(HTTP2_SINK_PROPERTIES_PREFIX, "output-message.");

    public static final String STATIC_HTTP2_SINK_MESSAGE_OUTPUT_ENDPOINT = createProperty(STATIC_HTTP2_SINK_OUTPUT_MESSAGE_CONFIGURATION_PREFIX, "target_endpoint");
    public static final String STATIC_HTTP2_SINK_MESSAGE_OUTPUT_HOST = createProperty(STATIC_HTTP2_SINK_OUTPUT_MESSAGE_CONFIGURATION_PREFIX, "target_host");
    public static final String STATIC_HTTP2_SINK_MESSAGE_OUTPUT_PORT = createProperty(STATIC_HTTP2_SINK_OUTPUT_MESSAGE_CONFIGURATION_PREFIX, "target_port");
    public static final String STATIC_HTTP2_SINK_MESSAGE_OUTPUT_SCHEME = createProperty(STATIC_HTTP2_SINK_OUTPUT_MESSAGE_CONFIGURATION_PREFIX, "target_scheme");
    public static final String STATIC_HTTP2_SINK_CONTENT_TYPE = createProperty(STATIC_HTTP2_SINK_OUTPUT_MESSAGE_CONFIGURATION_PREFIX, "content_type");
    public static final String STATIC_HTTP2_SINK_MESSAGE_PROPERTIES_MAPPING = createProperty(STATIC_HTTP2_SINK_OUTPUT_MESSAGE_CONFIGURATION_PREFIX, "properties_mapping");
    public static final String STATIC_HTTP2_SINK_MESSAGE_HEADERS_MAPPING = createProperty(STATIC_HTTP2_SINK_OUTPUT_MESSAGE_CONFIGURATION_PREFIX, "headers_mapping");

    public static final String STATIC_HTTP2_SINK_PROCESSING_CONFIGURATION_PREFIX = createProperty(HTTP2_SINK_PROPERTIES_PREFIX, "processing.");

    public static final String STATIC_HTTP2_SINK_DISABLE_ASYNC_SENDING = createProperty(STATIC_HTTP2_SINK_PROCESSING_CONFIGURATION_PREFIX, "disable_async");
    public static final String STATIC_HTTP2_SINK_ON_SENDING_ERROR_POLICY = createProperty(STATIC_HTTP2_SINK_PROCESSING_CONFIGURATION_PREFIX, "on_error_policy");
    public static final String STATIC_HTTP2_SINK_MAX_RETRIES_ON_ERROR = createProperty(STATIC_HTTP2_SINK_PROCESSING_CONFIGURATION_PREFIX, "max_retries");
    public static final String STATIC_HTTP2_SINK_SYNC_SENDING_WAIT_MS = createProperty(STATIC_HTTP2_SINK_PROCESSING_CONFIGURATION_PREFIX, "sync_wait_ms");

    public static final String STATIC_HTTP2_SINK_CLIENT_CONFIGURATION_PREFIX = createProperty(HTTP2_SINK_PROPERTIES_PREFIX, "client.");

    public static final String STATIC_HTTP2_SINK_CLIENT_GRACEFUL_SHUTDOWN = createProperty(STATIC_HTTP2_SINK_CLIENT_CONFIGURATION_PREFIX, "graceful_shutdown");

    public static final String STATIC_HTTP2_SINK_CLIENT_CONNECTION_CONFIGURATION_PREFIX = createProperty(STATIC_HTTP2_SINK_CLIENT_CONFIGURATION_PREFIX, "connection.");

    public static final String STATIC_HTTP2_SINK_IDLE_TIMEOUT_MS = createProperty(STATIC_HTTP2_SINK_CLIENT_CONNECTION_CONFIGURATION_PREFIX, "idle_timeout_ms");
    public static final String STATIC_HTTP2_SINK_VALIDATE_AFTER_INACTIVITY_MS = createProperty(STATIC_HTTP2_SINK_CLIENT_CONNECTION_CONFIGURATION_PREFIX, "validate_after_inactivity_interval_ms");
    public static final String STATIC_HTTP2_SINK_CONN_SOCKET_TIMEOUT_MS = createProperty(STATIC_HTTP2_SINK_CLIENT_CONNECTION_CONFIGURATION_PREFIX, "socket_timeout_ms");
    public static final String STATIC_HTTP2_SINK_CONN_CONNECT_TIMEOUT_MS = createProperty(STATIC_HTTP2_SINK_CLIENT_CONNECTION_CONFIGURATION_PREFIX, "connect_timeout_ms");
    public static final String STATIC_HTTP2_SINK_CONN_TTL_MS = createProperty(STATIC_HTTP2_SINK_CLIENT_CONNECTION_CONFIGURATION_PREFIX, "time_to_live_ms");

    public static final String STATIC_HTTP2_SINK_CLIENT_AUTH_CONFIGURATION_PREFIX = createProperty(STATIC_HTTP2_SINK_CLIENT_CONFIGURATION_PREFIX, "auth.");

    public static final String STATIC_HTTP2_SINK_AUTH_PROVIDER = createProperty(STATIC_HTTP2_SINK_CLIENT_AUTH_CONFIGURATION_PREFIX, "auth_provider");
    public static final String STATIC_HTTP2_SINK_AUTH_REALM = createProperty(STATIC_HTTP2_SINK_CLIENT_AUTH_CONFIGURATION_PREFIX, "realm");
    public static final String STATIC_HTTP2_SINK_AUTH_USERNAME = createProperty(STATIC_HTTP2_SINK_CLIENT_AUTH_CONFIGURATION_PREFIX, "basic_user_name");
    public static final String STATIC_HTTP2_SINK_AUTH_PWD = createProperty(STATIC_HTTP2_SINK_CLIENT_AUTH_CONFIGURATION_PREFIX, "basic_password");

    public static final String STATIC_HTTP2_SINK_CLIENT_TLS_CONFIGURATION_PREFIX = createProperty(STATIC_HTTP2_SINK_CLIENT_CONFIGURATION_PREFIX, "tls.");

    public static final String STATIC_HTTP2_SINK_TLS_ENABLED = createProperty(STATIC_HTTP2_SINK_CLIENT_TLS_CONFIGURATION_PREFIX, "use_tls");
    public static final String STATIC_HTTP2_SINK_MTLS_ENABLED = createProperty(STATIC_HTTP2_SINK_CLIENT_TLS_CONFIGURATION_PREFIX, "use_mtls");
    public static final String STATIC_HTTP2_SINK_TLS_PROTOCOL = createProperty(STATIC_HTTP2_SINK_CLIENT_TLS_CONFIGURATION_PREFIX, "protocol");
    public static final String STATIC_HTTP2_SINK_TLS_TRUSTSTORE_TYPE = createProperty(STATIC_HTTP2_SINK_CLIENT_TLS_CONFIGURATION_PREFIX, "truststore_type");
    public static final String STATIC_HTTP2_SINK_TLS_TRUSTSTORE_LOCATION = createProperty(STATIC_HTTP2_SINK_CLIENT_TLS_CONFIGURATION_PREFIX, "truststore_location");
    public static final String STATIC_HTTP2_SINK_TLS_TRUSTSTORE_PWD = createProperty(STATIC_HTTP2_SINK_CLIENT_TLS_CONFIGURATION_PREFIX, "truststore_password");
    public static final String STATIC_HTTP2_SINK_TLS_KEYSTORE_TYPE = createProperty(STATIC_HTTP2_SINK_CLIENT_TLS_CONFIGURATION_PREFIX, "keystore_type");
    public static final String STATIC_HTTP2_SINK_TLS_KEYSTORE_LOCATION = createProperty(STATIC_HTTP2_SINK_CLIENT_TLS_CONFIGURATION_PREFIX, "keystore_location");
    public static final String STATIC_HTTP2_SINK_TLS_KEYSTORE_PWD = createProperty(STATIC_HTTP2_SINK_CLIENT_TLS_CONFIGURATION_PREFIX, "keystore_password");
    public static final String STATIC_HTTP2_SINK_TLS_KEYSTORE_KEY_PWD = createProperty(STATIC_HTTP2_SINK_CLIENT_TLS_CONFIGURATION_PREFIX, "keystore_key_password");

    public static final String STATIC_HTTP2_SINK_CLIENT_IO_CONFIGURATION_PREFIX = createProperty(STATIC_HTTP2_SINK_CLIENT_CONFIGURATION_PREFIX, "io.");

    public static final String STATIC_HTTP2_SINK_IO_THREAD_COUNT = createProperty(STATIC_HTTP2_SINK_CLIENT_IO_CONFIGURATION_PREFIX, "thread_count");
    public static final String STATIC_HTTP2_SINK_IO_SEND_BUFFER_SIZE = createProperty(STATIC_HTTP2_SINK_CLIENT_IO_CONFIGURATION_PREFIX, "send_buffer_size");
    public static final String STATIC_HTTP2_SINK_IO_LINGER_MS = createProperty(STATIC_HTTP2_SINK_CLIENT_IO_CONFIGURATION_PREFIX, "linger_ms");
    public static final String STATIC_HTTP2_SINK_IO_TCP_NO_DELAY = createProperty(STATIC_HTTP2_SINK_CLIENT_IO_CONFIGURATION_PREFIX, "tcp_no_delay");
    public static final String STATIC_HTTP2_SINK_IO_TCP_KEEP_ALIVE_PROBE_INTERVAL_MS = createProperty(STATIC_HTTP2_SINK_CLIENT_IO_CONFIGURATION_PREFIX, "tcp_keep_alive_probe_interval_ms");
    public static final String STATIC_HTTP2_SINK_IO_TCP_IDLE_TIMEOUT_MS = createProperty(STATIC_HTTP2_SINK_CLIENT_IO_CONFIGURATION_PREFIX, "tcp_idle_timeout_ms");
    public static final String STATIC_HTTP2_SINK_IO_MAX_KEEP_ALIVE_PROBES_BEFORE_DROP = createProperty(STATIC_HTTP2_SINK_CLIENT_IO_CONFIGURATION_PREFIX, "tcp_max_keep_alive_probes_before_drop");
    public static final String STATIC_HTTP2_SINK_IO_SOCKS_PROXY_HOST = createProperty(STATIC_HTTP2_SINK_CLIENT_IO_CONFIGURATION_PREFIX, "socks_proxy_host");
    public static final String STATIC_HTTP2_SINK_IO_SOCKS_PROXY_PORT = createProperty(STATIC_HTTP2_SINK_CLIENT_IO_CONFIGURATION_PREFIX, "socks_proxy_port");
    public static final String STATIC_HTTP2_SINK_IO_SOCKS_PROXY_USERNAME = createProperty(STATIC_HTTP2_SINK_CLIENT_IO_CONFIGURATION_PREFIX, "socks_proxy_username");
    public static final String STATIC_HTTP2_SINK_IO_SOCKS_PROXY_PWD = createProperty(STATIC_HTTP2_SINK_CLIENT_IO_CONFIGURATION_PREFIX, "socks_proxy_password");
    public static final String STATIC_HTTP2_SINK_IO_SOCKET_TIMEOUT_MS = createProperty(STATIC_HTTP2_SINK_CLIENT_IO_CONFIGURATION_PREFIX, "socket_timeout_ms");

    public static final String STATIC_HTTP2_SINK_CLIENT_REQUEST_CONFIGURATION_PREFIX = createProperty(STATIC_HTTP2_SINK_CLIENT_CONFIGURATION_PREFIX, "request.");

    public static final String STATIC_HTTP2_SINK_REQUEST_MAX_RETRIES = createProperty(STATIC_HTTP2_SINK_CLIENT_REQUEST_CONFIGURATION_PREFIX, "max_retries");
    public static final String STATIC_HTTP2_SINK_REQUEST_RETRY_INTERVAL_MS = createProperty(STATIC_HTTP2_SINK_CLIENT_REQUEST_CONFIGURATION_PREFIX, "retry_interval_ms");
    public static final String STATIC_HTTP2_SINK_REQUEST_AUTH_ENABLED = createProperty(STATIC_HTTP2_SINK_CLIENT_REQUEST_CONFIGURATION_PREFIX, "auth_enabled");
    public static final String STATIC_HTTP2_SINK_REQUEST_CIRCULAR_REDIRECTS_ALLOWED = createProperty(STATIC_HTTP2_SINK_CLIENT_REQUEST_CONFIGURATION_PREFIX, "circular_redirects_allowed");
    public static final String STATIC_HTTP2_SINK_REQUEST_KEEP_ALIVE_MS = createProperty(STATIC_HTTP2_SINK_CLIENT_REQUEST_CONFIGURATION_PREFIX, "keep_alive_ms");
    public static final String STATIC_HTTP2_SINK_REQUEST_CONN_MANAGER_TIMEOUT_MS = createProperty(STATIC_HTTP2_SINK_CLIENT_REQUEST_CONFIGURATION_PREFIX, "conn_manager_timeout_ms");
    public static final String STATIC_HTTP2_SINK_REQUEST_DISABLE_COMPRESSION = createProperty(STATIC_HTTP2_SINK_CLIENT_REQUEST_CONFIGURATION_PREFIX, "disable_compression");
    public static final String STATIC_HTTP2_SINK_REQUEST_EXPECT_CONTINUE = createProperty(STATIC_HTTP2_SINK_CLIENT_REQUEST_CONFIGURATION_PREFIX, "expect_continue");
    public static final String STATIC_HTTP2_SINK_REQUEST_MAX_REDIRECTS = createProperty(STATIC_HTTP2_SINK_CLIENT_REQUEST_CONFIGURATION_PREFIX, "max_redirects");
    public static final String STATIC_HTTP2_SINK_REQUEST_DISABLE_PROTOCOL_UPGRADE = createProperty(STATIC_HTTP2_SINK_CLIENT_REQUEST_CONFIGURATION_PREFIX, "disable_protocol_upgrade");
    public static final String STATIC_HTTP2_SINK_REQUEST_MAX_FRAME_SIZE = createProperty(STATIC_HTTP2_SINK_CLIENT_REQUEST_CONFIGURATION_PREFIX, "max_frame_size");
    public static final String STATIC_HTTP2_SINK_REQUEST_MAX_CONCURRENT_STREAMS = createProperty(STATIC_HTTP2_SINK_CLIENT_REQUEST_CONFIGURATION_PREFIX, "max_concurrent_streams");
    public static final String STATIC_HTTP2_SINK_REQUEST_INITIAL_WINDOW_SIZE = createProperty(STATIC_HTTP2_SINK_CLIENT_REQUEST_CONFIGURATION_PREFIX, "initial_window_size");

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
