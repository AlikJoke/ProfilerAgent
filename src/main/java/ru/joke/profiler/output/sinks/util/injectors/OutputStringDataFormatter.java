package ru.joke.profiler.output.sinks.util.injectors;

import ru.joke.profiler.configuration.util.ProfilingTimeUnit;
import ru.joke.profiler.output.sinks.OutputData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class OutputStringDataFormatter extends OutputPropertiesInjector<String> {

    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd hh:mm:ss.[SSSSSS][SSS]";

    private static final String PROPERTY_START = "${";
    private static final String PROPERTY_END = "}";
    private static final char FORMAT_DELIMITER = ':';

    private final DateTimeFormatter currentTimestampFormatter;
    private final TimeUnit enterTimestampUnit;
    private final TimeUnit elapsedTimeUnit;
    private final String outputDataPattern;

    public OutputStringDataFormatter(final String outputDataPattern) {
        super(collectRuntimeInjectedProperties(outputDataPattern));
        final StringBuilder patternBuilder = new StringBuilder(outputDataPattern);
        final String currentTimestampFormat = extractTimestampFormat(patternBuilder);
        this.currentTimestampFormatter = currentTimestampFormat == null ? null : DateTimeFormatter.ofPattern(currentTimestampFormat);
        this.enterTimestampUnit = extractEnterTimestampUnit(patternBuilder);
        this.elapsedTimeUnit = extractElapsedTimeUnit(patternBuilder);
        this.outputDataPattern = injectPredefinedProperties(patternBuilder).toString();
    }

    public Supplier<String> formatLater(final OutputData outputData) {
        final String method = outputData.method();
        final long elapsedTime = outputData.methodElapsedTime();
        final long methodEnterTimestamp = outputData.methodEnterTimestamp();
        final String traceId = outputData.traceId();
        final int depth = outputData.depth();
        final String threadName = outputData.thread();
        final LocalDateTime timestamp = outputData.timestamp();
        final String spanId = outputData.spanId();
        final String parentSpanId = outputData.parentSpanId();

        return () -> format(traceId, depth, method, methodEnterTimestamp, elapsedTime, threadName, timestamp, spanId, parentSpanId);
    }

    public String format(final OutputData outputData) {
        return format(
                outputData.traceId(),
                outputData.depth(),
                outputData.method(),
                outputData.methodEnterTimestamp(),
                outputData.methodElapsedTime(),
                outputData.thread(),
                outputData.timestamp(),
                outputData.spanId(),
                outputData.parentSpanId()
        );
    }

    @Override
    protected String injectMethodName(
            final String template,
            final String method,
            final String property,
            final int propertyIndex
    ) {
        return injectProperty(template, property, method);
    }

    @Override
    protected String injectMethodEnterTimestamp(
            final String template,
            final long methodEnterTimestamp,
            final String property,
            final int propertyIndex
    ) {
        return injectProperty(template, property, String.valueOf(methodEnterTimestamp));
    }

    @Override
    protected String injectMethodElapsedTime(
            final String template,
            final long methodElapsedTime,
            final String property,
            final int propertyIndex
    ) {
        return injectProperty(template, property, String.valueOf(methodElapsedTime));
    }

    @Override
    protected String injectTraceId(
            final String template,
            final String traceId,
            final String property,
            final int propertyIndex
    ) {
        return injectProperty(template, property, traceId);
    }

    @Override
    protected String injectDepth(
            final String template,
            final int depth,
            final String property,
            final int propertyIndex
    ) {
        return injectProperty(template, property, String.valueOf(depth));
    }

    @Override
    protected String injectSpanId(
            final String template,
            final String spanId,
            final String property,
            final int propertyIndex
    ) {
        return injectProperty(template, property, spanId);
    }

    @Override
    protected String injectParentSpanId(
            final String template,
            final String parentSpanId,
            final String property,
            final int propertyIndex
    ) {
        return injectProperty(template, property, parentSpanId);
    }

    @Override
    protected String injectIp(
            final String template,
            final String ip,
            final String property,
            final int propertyIndex
    ) {
        return injectProperty(template, property, ip);
    }

    @Override
    protected String injectHost(
            final String template,
            final String host,
            final String property,
            final int propertyIndex
    ) {
        return injectProperty(template, property, String.valueOf(host));
    }

    @Override
    protected String injectSource(
            final String template,
            final String source,
            final String property,
            final int propertyIndex
    ) {
        return injectProperty(template, property, source);
    }

    @Override
    protected String injectSystemProperty(
            final String template,
            final String value,
            final String property,
            final int propertyIndex
    ) {
        return injectProperty(template, property, value);
    }

    @Override
    protected String injectThreadName(
            final String template,
            final String threadName,
            final String property,
            final int propertyIndex
    ) {
        return injectProperty(template, property, threadName);
    }

    @Override
    protected String injectCurrentTimestamp(
            final String template,
            final LocalDateTime timestamp,
            final String property,
            final int propertyIndex
    ) {
        return injectProperty(template, property, this.currentTimestampFormatter.format(timestamp));
    }

    private String format(
            final String traceId,
            final int depth,
            final String method,
            final long methodEnterTimestamp,
            final long elapsedTime,
            final String threadName,
            final LocalDateTime timestamp,
            final String spanId,
            final String parentSpanId
    ) {
        String result = injectProperty(this.outputDataPattern, THREAD_PROPERTY, threadName);
        result = injectProperty(result, METHOD_PROPERTY, method);
        result = injectProperty(result, METHOD_ENTER_TS_PROPERTY, String.valueOf(this.enterTimestampUnit.convert(methodEnterTimestamp, TimeUnit.NANOSECONDS)));
        result = injectProperty(result, METHOD_ELAPSED_TIME_PROPERTY, String.valueOf(this.elapsedTimeUnit.convert(elapsedTime, TimeUnit.NANOSECONDS)));
        result = injectProperty(result, SOURCE_PROPERTY, PROFILER_LABEL);
        result = injectProperty(result, TRACE_ID_PROPERTY, traceId);
        result = injectProperty(result, DEPTH_PROPERTY, String.valueOf(depth));
        result = injectProperty(result, SPAN_ID_PROPERTY, spanId);
        result = injectProperty(result, PARENT_SPAN_ID_PROPERTY, parentSpanId);

        if (this.currentTimestampFormatter != null) {
            result = injectProperty(result, CURRENT_TS_PROPERTY, this.currentTimestampFormatter.format(timestamp));
        }

        return result;
    }

    private String injectProperty(
            final String target,
            final String property,
            final String propertyValue
    ) {
        return target.replace(PROPERTY_START + property + PROPERTY_END, propertyValue);
    }

    private TimeUnit extractEnterTimestampUnit(final StringBuilder pattern) {
        final String result = extractPropertyFormat(pattern, METHOD_ENTER_TS_PROPERTY);
        return ProfilingTimeUnit.parse(result, ProfilingTimeUnit.NANOSECONDS).toJavaTimeUnit();
    }

    private TimeUnit extractElapsedTimeUnit(final StringBuilder pattern) {
        final String result = extractPropertyFormat(pattern, METHOD_ELAPSED_TIME_PROPERTY);
        return ProfilingTimeUnit.parse(result, ProfilingTimeUnit.NANOSECONDS).toJavaTimeUnit();
    }

    private String extractTimestampFormat(final StringBuilder pattern) {
        final String result = extractPropertyFormat(pattern, CURRENT_TS_PROPERTY);
        return result != null && result.isEmpty() ? DEFAULT_TIMESTAMP_FORMAT : result;
    }

    private String extractPropertyFormat(final StringBuilder pattern, final String propertyName) {
        final int propertyStartIndex = pattern.indexOf(PROPERTY_START + propertyName);
        if (propertyStartIndex == -1) {
            return null;
        }

        final int endPropertyIndex = pattern.indexOf(PROPERTY_END, propertyStartIndex + 1);
        final int startFormatIndex = propertyStartIndex + PROPERTY_START.length() + propertyName.length();
        final String result = pattern.substring(startFormatIndex, endPropertyIndex);
        pattern.delete(startFormatIndex, endPropertyIndex);

        return !result.isEmpty() && result.charAt(0) == FORMAT_DELIMITER ? result.substring(1) : result;
    }

    private StringBuilder injectPredefinedProperties(final StringBuilder pattern) {
        int startPropertyIndex = pattern.indexOf(PROPERTY_START);
        while (startPropertyIndex >= 0) {

            int endPropertyIndex = pattern.indexOf(PROPERTY_END, startPropertyIndex + 1);
            final String property = pattern.substring(startPropertyIndex + PROPERTY_START.length(), endPropertyIndex);
            final String propertyValue =
                    !property.isEmpty() && this.systemProperties.containsKey(property)
                            ? this.systemProperties.get(property)
                            : property.equalsIgnoreCase(HOST_PROPERTY)
                                ? this.host
                                : property.equalsIgnoreCase(IP_PROPERTY)
                                    ? this.ip
                                    : null;
            if (propertyValue != null) {
                pattern.replace(startPropertyIndex, endPropertyIndex + 1, propertyValue);
                endPropertyIndex = startPropertyIndex + propertyValue.length();
            }

            startPropertyIndex = pattern.indexOf(PROPERTY_START, endPropertyIndex);
        }

        return pattern;
    }

    private static Set<String> collectRuntimeInjectedProperties(final String pattern) {
        final Set<String> result = new HashSet<>();

        int startIndex = pattern.indexOf(PROPERTY_START);
        while (startIndex != -1) {
            final int endIndex = pattern.indexOf(PROPERTY_END, startIndex + 1);

            final String propertyPlaceholder = pattern.substring(startIndex, endIndex);
            final int formatIndex = propertyPlaceholder.indexOf(":");
            final String property = formatIndex != -1 ? propertyPlaceholder.substring(0, formatIndex) : propertyPlaceholder;
            result.add(property);

            startIndex = pattern.indexOf(PROPERTY_START, endIndex);
        }

        final Properties systemProperties = System.getProperties();
        return result
                .stream()
                .filter(p -> !systemProperties.containsKey(p) && !p.equals(HOST_PROPERTY) && !p.equals(IP_PROPERTY) && !p.equals(SOURCE_PROPERTY))
                .collect(Collectors.toSet());
    }
}
