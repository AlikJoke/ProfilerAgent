package ru.joke.profiler.core.output;

import ru.joke.profiler.core.output.handlers.OutputData;
import ru.joke.profiler.core.output.handlers.OutputDataSink;

import java.util.UUID;

public final class TracedExecutionTimeRegistrar extends ExecutionTimeRegistrar {

    private final ThreadLocal<TraceData> traceData = new ThreadLocal<>();
    private final ThreadLocal<OutputData> outputData = ThreadLocal.withInitial(OutputData::new);

    private final OutputDataSink outputSink;

    public TracedExecutionTimeRegistrar(final OutputDataSink outputSink) {
        this.outputSink = outputSink;
    }

    @Override
    public void registerMethodExit() {
        final TraceData methodTraceData = traceData.get();
        if (methodTraceData.currentDepth-- == 0) {
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
            methodTraceData.currentDepth++;
        }
    }

    @Override
    public void registerMethodExit(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime) {
        super.registerMethodExit(method, methodEnterTimestamp, methodElapsedTime);
        registerMethodExit();
    }

    @Override
    protected void write(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime) {
        final TraceData methodTraceData = traceData.get();

        final OutputData output = this.outputData.get();
        output.fill(
                method,
                methodElapsedTime,
                methodEnterTimestamp,
                methodTraceData.traceId,
                methodTraceData.currentDepth
        );

        this.outputSink.write(output);
    }

    private static class TraceData {

        private final String traceId;
        private int currentDepth;
        // TODO spanId

        private TraceData() {
            this.traceId = UUID.randomUUID().toString();
        }
    }
}
