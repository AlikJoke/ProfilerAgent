package ru.joke.profiler.core.output;

import ru.joke.profiler.core.configuration.DynamicProfilingConfiguration;
import ru.joke.profiler.core.configuration.DynamicProfilingConfigurationHolder;

import java.util.UUID;

import static ru.joke.profiler.core.output.ExecutionTimeRegistrarUtil.isProfilingApplied;

public final class TracedExecutionTimeRegistrar {

    private static final ThreadLocal<TraceData> traceData = new ThreadLocal<>();

    public static void registerMethodExit() {
        final TraceData methodTraceData = traceData.get();
        if (methodTraceData.currentSpanId-- == 0) {
            traceData.remove();
        }
    }

    @SuppressWarnings("unused")
    public static void registerMethodEnter() {
        final TraceData methodTraceData = traceData.get();
        if (methodTraceData == null) {
            traceData.set(new TraceData());
        } else {
            methodTraceData.currentSpanId++;
        }
    }

    public static void registerStatic(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime) {
        write(method, methodEnterTimestamp, methodElapsedTime);
    }

    @SuppressWarnings("unused")
    public static void registerDynamic(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime) {
        final DynamicProfilingConfigurationHolder dynamicConfigHolder = DynamicProfilingConfigurationHolder.getInstance();
        final DynamicProfilingConfiguration dynamicConfig = dynamicConfigHolder.getDynamicConfiguration();
        if (dynamicConfig == null) {
            registerStatic(method, methodEnterTimestamp, methodElapsedTime);
            return;
        }

        if (!isProfilingApplied(method, methodElapsedTime, dynamicConfig)) {
            registerMethodExit();
            return;
        }

        write(method, methodEnterTimestamp, methodElapsedTime);
    }

    private static void write(final String method, final long methodEnterTimestamp, final long methodElapsedTime) {
        final TraceData methodTraceData = traceData.get();
        final int spanId = methodTraceData.currentSpanId--;
        if (spanId == 0) {
            traceData.remove();
        }

        // TODO
        System.out.println(methodTraceData.traceId + ":" + spanId + ":" + method + ":" + methodEnterTimestamp + ":" + methodElapsedTime);
    }

    private static class TraceData {

        private final String traceId;
        private int currentSpanId;

        private TraceData() {
            this.traceId = UUID.randomUUID().toString();
        }
    }
}
