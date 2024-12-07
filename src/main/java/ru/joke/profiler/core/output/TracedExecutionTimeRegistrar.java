package ru.joke.profiler.core.output;

import java.util.UUID;

public final class TracedExecutionTimeRegistrar extends ExecutionTimeRegistrar {

    private final ThreadLocal<TraceData> traceData = new ThreadLocal<>();

    @Override
    public void registerMethodExit() {
        final TraceData methodTraceData = traceData.get();
        if (methodTraceData.currentSpanId-- == 0) {
            traceData.remove();
        }
    }

    @Override
    protected boolean isRegistrationOccurredOnTrace() {
        return traceData.get() != null;
    }

    @Override
    public void registerMethodEnter(final String method) {
        final TraceData methodTraceData = traceData.get();
        if (methodTraceData == null) {
            traceData.set(new TraceData());
        } else {
            methodTraceData.currentSpanId++;
        }
    }

    @Override
    public void registerMethodExit(String method, long methodEnterTimestamp, long methodElapsedTime) {
        super.registerMethodExit(method, methodEnterTimestamp, methodElapsedTime);
        registerMethodExit();
    }

    @Override
    protected void write(final String method, final long methodEnterTimestamp, final long methodElapsedTime) {
        final TraceData methodTraceData = traceData.get();
        final int spanId = methodTraceData.currentSpanId;

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
