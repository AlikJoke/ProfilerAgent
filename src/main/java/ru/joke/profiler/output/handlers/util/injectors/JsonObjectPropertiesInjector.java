package ru.joke.profiler.output.handlers.util.injectors;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JsonObjectPropertiesInjector extends OutputPropertiesInjector<StringBuilder> {

    private final Map<String, String> mappingMetadata;

    private static final Map<String, String> defaultProperties = Stream.of(
            CURRENT_TS_PROPERTY,
            THREAD_PROPERTY,
            TRACE_ID_PROPERTY,
            DEPTH_PROPERTY,
            METHOD_PROPERTY,
            METHOD_ENTER_TS_PROPERTY,
            METHOD_ELAPSED_TIME_PROPERTY
    ).collect(Collectors.toMap(Function.identity(), Function.identity()));

    public JsonObjectPropertiesInjector(final Map<String, String> mappingMetadata) {
        super(mappingMetadata.isEmpty() ? defaultProperties.keySet() : mappingMetadata.keySet());
        this.mappingMetadata = mappingMetadata.isEmpty() ? defaultProperties : mappingMetadata;
    }

    @Override
    protected StringBuilder injectMethodName(
            final StringBuilder template,
            final String method,
            final String property,
            final int propertyIndex
    ) {
        appendFieldName(template, property);
        appendStringValue(template, method);
        return template;
    }

    @Override
    protected StringBuilder injectMethodEnterTimestamp(
            final StringBuilder template,
            final long methodEnterTimestamp,
            final String property,
            final int propertyIndex
    ) {
        appendFieldName(template, property);
        appendLongValue(template, methodEnterTimestamp);
        return template;
    }

    @Override
    protected StringBuilder injectMethodElapsedTime(
            final StringBuilder template,
            final long methodElapsedTime,
            final String property,
            final int propertyIndex
    ) {
        appendFieldName(template, property);
        appendLongValue(template, methodElapsedTime);
        return template;
    }

    @Override
    protected StringBuilder injectTraceId(
            final StringBuilder template,
            final String traceId,
            final String property,
            final int propertyIndex
    ) {
        appendFieldName(template, property);
        appendStringValue(template, traceId);
        return template;
    }

    @Override
    protected StringBuilder injectDepth(
            final StringBuilder template,
            final int depth,
            final String property,
            final int propertyIndex
    ) {
        appendFieldName(template, property);
        appendIntValue(template, depth);
        return template;
    }

    @Override
    protected StringBuilder injectIp(
            final StringBuilder template,
            final String ip,
            final String property,
            final int propertyIndex
    ) {
        appendFieldName(template, property);
        appendStringValue(template, ip);
        return template;
    }

    @Override
    protected StringBuilder injectHost(
            final StringBuilder template,
            final String host,
            final String property,
            final int propertyIndex
    ) {
        appendFieldName(template, property);
        appendStringValue(template, host);
        return template;
    }

    @Override
    protected StringBuilder injectSource(
            final StringBuilder template,
            final String source,
            final String property,
            final int propertyIndex
    ) {
        appendFieldName(template, property);
        appendStringValue(template, source);
        return template;
    }

    @Override
    protected StringBuilder injectSystemProperty(
            final StringBuilder template,
            final String value,
            final String property,
            final int propertyIndex
    ) {
        appendFieldName(template, property);
        appendStringValue(template, value);
        return template;
    }

    @Override
    protected StringBuilder injectThreadName(
            final StringBuilder template,
            final String threadName,
            final String property,
            final int propertyIndex
    ) {
        appendFieldName(template, property);
        appendStringValue(template, threadName);
        return template;
    }

    @Override
    protected StringBuilder injectCurrentTimestamp(
            final StringBuilder template,
            final LocalDateTime timestamp,
            final String property,
            final int propertyIndex
    ) {
        appendFieldName(template, property);
        appendStringValue(template, timestamp.toString());
        return template;
    }

    @Override
    protected StringBuilder onStart(final StringBuilder template) {
        return template.append("{");
    }

    @Override
    protected StringBuilder onFinish(final StringBuilder template) {
        return (template.length() > 1
                ? template.deleteCharAt(template.length() - 1)
                : template).append("}");
    }

    private void appendFieldName(final StringBuilder template, final String property) {
        template.append("\"")
                .append(this.mappingMetadata.get(property))
                .append("\":");
    }

    private void appendStringValue(final StringBuilder template, final String value) {
        template.append("\"")
                .append(value)
                .append("\"")
                .append(",");
    }

    private void appendLongValue(final StringBuilder template, final long value) {
        template.append(value).append(",");
    }

    private void appendIntValue(final StringBuilder template, final int value) {
        template.append(value).append(",");
    }
}
