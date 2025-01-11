package ru.joke.profiler.output;

import ru.joke.profiler.output.sinks.OutputData;
import ru.joke.profiler.output.sinks.OutputDataSink;

import java.util.UUID;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

public final class TracedExecutionTimeRegistrar extends ExecutionTimeRegistrar {

    private final ThreadLocal<OutputData> outputData;
    private final OutputDataSink<OutputData> outputSink;

    public TracedExecutionTimeRegistrar(final OutputDataSink<OutputData> outputSink) {
        this.outputSink = checkNotNull(outputSink, "outputSink");
        this.outputData = ThreadLocal.withInitial(OutputData::new);
    }

    @Override
    public void registerMethodEnter(final String method) {
        final OutputData methodData = this.outputData.get();
        if (methodData.traceId() == null) {
            methodData.withTraceId(generateTraceId(method));
        } else {
            methodData.withDepth(methodData.depth() + 1);
        }

        methodData.withSpan(generateSpanId(method, methodData.depth(), methodData.traceId()));
    }

    @Override
    public void registerMethodExit() {
        registerMethodExit(false);
    }

    @Override
    protected boolean isRegistrationOccurredOnTrace() {
        return this.outputData.get().traceId() != null;
    }

    @Override
    public void registerMethodExit(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime
    ) {
        try {
            super.registerMethodExit(method, methodEnterTimestamp, methodElapsedTime);
        } finally {
            registerMethodExit(true);
        }
    }

    @Override
    protected void write(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime
    ) {
        final long startWriteTime = System.nanoTime();

        final OutputData output = this.outputData.get();
        try {
            output.fill(
                    method,
                    methodElapsedTime,
                    methodEnterTimestamp,
                    output.traceId(),
                    output.depth(),
                    output.pollLastSpan(),
                    output.peekLastSpan()
            );

            this.outputSink.write(output);
        } finally {
            output.increaseOverhead(System.nanoTime() - startWriteTime);
        }
    }

    private void registerMethodExit(final boolean isOutputRegistered) {
        final OutputData methodData = outputData.get();
        int currentDepth = methodData.depth();
        methodData.withDepth(--currentDepth);
        if (currentDepth == -1) {
            methodData.withTraceId(null);
            methodData.withDepth(0);
        }

        if (!isOutputRegistered) {
            methodData.pollLastSpan();
        }
    }

    private String generateTraceId(final String method) {
        final UUID uuid = UUID.randomUUID();
        final long uuidLsb = uuid.getLeastSignificantBits();
        final long result = (uuidLsb ^ method.hashCode()) * 3141592653589793L + System.nanoTime();
        return uuid + "-" + Long.toHexString(result);
    }

    private String generateSpanId(
            final String method,
            final int depth,
            final String traceId
    ) {
        long result = method.hashCode();
        result = (result << 13) ^ (result >> 7) ^ depth;
        result = (result << 29) ^ (result >> 11) ^ System.nanoTime();
        result = (result << 17) ^ (result >> 9) ^ traceId.hashCode();

        result = result ^ (result >>> 32);
        result = result * 0x4a6d138479645723L;
        result = result ^ (result >>> 35);
        result = result * 0x7c9b23a8d456f123L;
        result = result ^ (result >>> 28);
        
        return Long.toHexString(result);
    }
}
