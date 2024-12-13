package ru.joke.profiler.core.output.handlers;

import ru.joke.profiler.core.ProfilerException;
import ru.joke.profiler.core.configuration.ProfilingTimeUnit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class OutputStringDataFormatter {

    private static final String PROFILER_LABEL = "joke-profiler";
    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd hh:mm:ss.[SSSSSS][SSS]";

    private static final String PROPERTY_START = "${";
    private static final String PROPERTY_END = "}";
    private static final char FORMAT_DELIMITER = ':';

    private static final String CURRENT_TS_PROPERTY = "current_ts";
    private static final String THREAD_PROPERTY = "thread";
    private static final String METHOD_PROPERTY = "method";
    private static final String ENTER_TS_PROPERTY = "enter_ts";
    private static final String ELAPSED_TIME_PROPERTY = "elapsed";
    private static final String TRACE_ID_PROPERTY = "trace_id";
    private static final String DEPTH_PROPERTY = "depth";
    private static final String SOURCE_PROPERTY = "source";
    private static final String HOST_PROPERTY = "host";
    private static final String IP_PROPERTY = "ip";

    private final DateTimeFormatter currentTimestampFormatter;
    private final TimeUnit enterTimestampUnit;
    private final TimeUnit elapsedTimeUnit;
    private final String outputDataPattern;

    public OutputStringDataFormatter(final String outputDataPattern) {
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
        final String threadName = Thread.currentThread().getName();
        final LocalDateTime timestamp = LocalDateTime.now();

        return () -> format(traceId, depth, method, methodEnterTimestamp, elapsedTime, threadName, timestamp);
    }

    public String format(final OutputData outputData) {
        return format(
                outputData.traceId(),
                outputData.depth(),
                outputData.method(),
                outputData.methodEnterTimestamp(),
                outputData.methodElapsedTime(),
                Thread.currentThread().getName(),
                LocalDateTime.now()
        );
    }

    private String format(
            final String traceId,
            final int depth,
            final String method,
            final long methodEnterTimestamp,
            final long elapsedTime,
            final String threadName,
            final LocalDateTime timestamp) {
        String result = injectProperty(this.outputDataPattern, THREAD_PROPERTY, threadName);
        result = injectProperty(result, METHOD_PROPERTY, method);
        result = injectProperty(result, ENTER_TS_PROPERTY, String.valueOf(this.enterTimestampUnit.convert(methodEnterTimestamp, TimeUnit.NANOSECONDS)));
        result = injectProperty(result, ELAPSED_TIME_PROPERTY, String.valueOf(this.elapsedTimeUnit.convert(elapsedTime, TimeUnit.NANOSECONDS)));
        result = injectProperty(result, SOURCE_PROPERTY, PROFILER_LABEL);
        if (this.currentTimestampFormatter != null) {
            result = injectProperty(result, CURRENT_TS_PROPERTY, this.currentTimestampFormatter.format(timestamp));
        }

        if (traceId != null) {
            result = injectProperty(result, TRACE_ID_PROPERTY, traceId);
            result = injectProperty(result, DEPTH_PROPERTY, String.valueOf(depth));
        }

        return result;
    }

    private String injectProperty(final String target, final String property, final String propertyValue) {
        return target.replace(PROPERTY_START + property + PROPERTY_END, propertyValue);
    }

    private TimeUnit extractEnterTimestampUnit(final StringBuilder pattern) {
        final String result = extractPropertyFormat(pattern, ENTER_TS_PROPERTY);
        return ProfilingTimeUnit.parse(result, ProfilingTimeUnit.NANOSECONDS).toJavaTimeUnit();
    }

    private TimeUnit extractElapsedTimeUnit(final StringBuilder pattern) {
        final String result = extractPropertyFormat(pattern, ELAPSED_TIME_PROPERTY);
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
        final Properties properties = System.getProperties();
        while (startPropertyIndex >= 0) {

            int endPropertyIndex = pattern.indexOf(PROPERTY_END, startPropertyIndex + 1);
            final String property = pattern.substring(startPropertyIndex + PROPERTY_START.length(), endPropertyIndex);
            final String propertyValue =
                    !property.isEmpty() && properties.containsKey(property)
                            ? properties.getProperty(property)
                            : property.equalsIgnoreCase(HOST_PROPERTY)
                                ? findCurrentHost()
                                : property.equalsIgnoreCase(IP_PROPERTY)
                                    ? findCurrentHostAddress()
                                    : null;
            if (propertyValue != null) {
                pattern.replace(startPropertyIndex, endPropertyIndex + 1, propertyValue);
                endPropertyIndex = startPropertyIndex + propertyValue.length();
            }

            startPropertyIndex = pattern.indexOf(PROPERTY_START, endPropertyIndex);
        }

        return pattern;
    }

    private String findCurrentHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            throw new ProfilerException(ex);
        }
    }

    private String findCurrentHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            throw new ProfilerException(ex);
        }
    }
}
