package ru.joke.profiler.output.sinks.util.injectors;

import ru.joke.profiler.output.sinks.OutputData;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.*;

public abstract class OutputPropertiesInjector<T> {

    public static final String CURRENT_TS_PROPERTY = "current_ts";
    public static final String THREAD_PROPERTY = "thread";

    public static final String METHOD_PROPERTY = "method";
    public static final String TRACE_ID_PROPERTY = "trace_id";
    public static final String DEPTH_PROPERTY = "depth";
    public static final String METHOD_ELAPSED_TIME_PROPERTY = "elapsed";
    public static final String METHOD_ENTER_TS_PROPERTY = "enter_ts";
    public static final String SPAN_ID_PROPERTY = "span_id";
    public static final String PARENT_SPAN_ID_PROPERTY = "parent_span_id";

    public static final String SOURCE_PROPERTY = "source";
    public static final String HOST_PROPERTY = "host";
    public static final String IP_PROPERTY = "ip";

    protected static final String PROFILER_LABEL = "joke-profiler";
    private static final String UNKNOWN_FALLBACK = "unknown";

    protected final Set<String> properties;
    protected final String host;
    protected final String ip;
    protected final Map<String, String> systemProperties;

    protected OutputPropertiesInjector(final Set<String> properties) {
        this.properties = Collections.unmodifiableSet(properties);
        this.host = properties.contains(HOST_PROPERTY) ? findCurrentHost() : null;
        this.ip = properties.contains(IP_PROPERTY) ? findCurrentHostAddress() : null;
        this.systemProperties = new HashMap<>();
        final Properties systemProperties = System.getProperties();
        systemProperties
                .stringPropertyNames()
                .forEach(propertyName -> this.systemProperties.put(propertyName, systemProperties.getProperty(propertyName)));
    }

    public T inject(final T template, final OutputData data) {
        int index = 0;
        T result = onStart(template);
        for (final String property : this.properties) {
            result = injectProperty(result, data, property, ++index);
        }

        return onFinish(result);
    }

    protected T onStart(final T template) {
        return template;
    }

    protected T onFinish(final T template) {
        return template;
    }

    protected abstract T injectMethodName(
            final T template,
            final String method,
            final String property,
            final int propertyIndex
    );

    protected abstract T injectMethodEnterTimestamp(
            final T template,
            final long methodEnterTimestamp,
            final String property,
            final int propertyIndex
    );

    protected abstract T injectMethodElapsedTime(
            final T template,
            final long methodElapsedTime,
            final String property,
            final int propertyIndex
    );

    protected abstract T injectTraceId(
            final T template,
            final String traceId,
            final String property,
            final int propertyIndex
    );

    protected abstract T injectDepth(
            final T template,
            final int depth,
            final String property,
            final int propertyIndex
    );

    protected abstract T injectSpanId(
            final T template,
            final String spanId,
            final String property,
            final int propertyIndex
    );

    protected abstract T injectParentSpanId(
            final T template,
            final String parentSpanId,
            final String property,
            final int propertyIndex
    );

    protected abstract T injectIp(
            final T template,
            final String ip,
            final String property,
            final int propertyIndex
    );

    protected abstract T injectHost(
            final T template,
            final String host,
            final String property,
            final int propertyIndex
    );

    protected abstract T injectSource(
            final T template,
            final String source,
            final String property,
            final int propertyIndex
    );

    protected abstract T injectSystemProperty(
            final T template,
            final String value,
            final String property,
            final int propertyIndex
    );

    protected abstract T injectThreadName(
            final T template,
            final String threadName,
            final String property,
            final int propertyIndex
    );

    protected abstract T injectCurrentTimestamp(
            final T template,
            final LocalDateTime timestamp,
            final String property,
            final int propertyIndex
    );

    private T injectProperty(
            final T template,
            final OutputData data,
            final String property,
            final int index
    ) {

        switch (property) {
            case METHOD_PROPERTY:
                return injectMethodName(template, data.method(), property, index);
            case METHOD_ENTER_TS_PROPERTY:
                return injectMethodEnterTimestamp(template, data.methodEnterTimestamp(), property, index);
            case METHOD_ELAPSED_TIME_PROPERTY:
                return injectMethodElapsedTime(template, data.methodElapsedTime(), property, index);
            case TRACE_ID_PROPERTY:
                return injectTraceId(template, data.traceId(), property, index);
            case DEPTH_PROPERTY:
                return injectDepth(template, data.depth(), property, index);
            case SPAN_ID_PROPERTY:
                return injectSpanId(template, data.spanId(), property, index);
            case PARENT_SPAN_ID_PROPERTY:
                return injectParentSpanId(template, data.parentSpanId(), property, index);
            case IP_PROPERTY:
                return injectIp(template, this.ip, property, index);
            case HOST_PROPERTY:
                return injectHost(template, this.host, property, index);
            case SOURCE_PROPERTY:
                return injectSource(template, PROFILER_LABEL, property, index);
            case THREAD_PROPERTY:
                return injectThreadName(template, data.thread(), property, index);
            case CURRENT_TS_PROPERTY:
                    return injectCurrentTimestamp(template, data.timestamp(), property, index);
            default:
                return injectSystemProperty(template, this.systemProperties.get(property), property, index);
        }
    }

    private String findCurrentHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return UNKNOWN_FALLBACK;
        }
    }

    private String findCurrentHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            return UNKNOWN_FALLBACK;
        }
    }
}
