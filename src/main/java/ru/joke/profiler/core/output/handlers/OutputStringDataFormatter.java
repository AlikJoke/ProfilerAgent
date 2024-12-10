package ru.joke.profiler.core.output.handlers;

import ru.joke.profiler.core.configuration.ProfilingTimeUnit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public final class OutputStringDataFormatter {

    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd hh:mm:ss.[SSSSSS][SSS]";

    private static final String PROPERTY_START = "${";
    private static final String PROPERTY_END = "}";

    private static final String CURRENT_TS_PROPERTY = "current_ts";
    private static final String THREAD_PROPERTY = "thread";
    private static final String METHOD_PROPERTY = "method";
    private static final String ENTER_TS_PROPERTY = "enter_ts";
    private static final String ELAPSED_TIME_PROPERTY = "elapsed";
    private static final String TRACE_ID_PROPERTY = "trace_id";
    private static final String DEPTH_PROPERTY = "depth";

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
        this.outputDataPattern = injectProperties(patternBuilder).toString();
    }

    public String format(final OutputData outputData) {

        String result = injectProperty(this.outputDataPattern, THREAD_PROPERTY, Thread.currentThread().getName());
        result = injectProperty(result, METHOD_PROPERTY, outputData.method());
        result = injectProperty(result, ENTER_TS_PROPERTY, String.valueOf(this.enterTimestampUnit.convert(outputData.methodEnterTimestamp(), TimeUnit.NANOSECONDS)));
        result = injectProperty(result, ELAPSED_TIME_PROPERTY, String.valueOf(this.elapsedTimeUnit.convert(outputData.methodElapsedTime(), TimeUnit.NANOSECONDS)));
        if (this.currentTimestampFormatter != null) {
            result = injectProperty(result, CURRENT_TS_PROPERTY, this.currentTimestampFormatter.format(LocalDateTime.now()));
        }

        if (outputData.traceId() != null) {
            result = injectProperty(result, TRACE_ID_PROPERTY, outputData.traceId());
            result = injectProperty(result, DEPTH_PROPERTY, String.valueOf(outputData.depth()));
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
        final String result = pattern.substring(propertyStartIndex + propertyName.length(), endPropertyIndex + 1);
        pattern.delete(propertyStartIndex + propertyName.length(), endPropertyIndex + 1);

        return result;
    }

    private StringBuilder injectProperties(final StringBuilder pattern) {
        int startPropertyIndex = pattern.indexOf(PROPERTY_START);
        final Properties properties = System.getProperties();
        while (startPropertyIndex >= 0) {

            int endPropertyIndex = pattern.indexOf(PROPERTY_END, startPropertyIndex + 1);
            final String property = pattern.substring(startPropertyIndex, endPropertyIndex + 1);
            final String propertyValue;
            if (!property.isEmpty() && (propertyValue = properties.getProperty(property)) != null) {
                pattern.replace(startPropertyIndex, endPropertyIndex + 1, propertyValue);
                endPropertyIndex = startPropertyIndex + propertyValue.length();
            }

            startPropertyIndex = pattern.indexOf(PROPERTY_START, endPropertyIndex);
        }

        return pattern;
    }
}
