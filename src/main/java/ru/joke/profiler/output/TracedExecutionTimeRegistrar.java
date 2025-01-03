package ru.joke.profiler.output;

import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.OutputDataSink;

import java.util.UUID;

public final class TracedExecutionTimeRegistrar extends ExecutionTimeRegistrar {

    private final ThreadLocal<OutputData> outputData;
    private final OutputDataSink<OutputData> outputSink;

    public TracedExecutionTimeRegistrar(final OutputDataSink<OutputData> outputSink) {
        this.outputSink = outputSink;
        this.outputData = ThreadLocal.withInitial(OutputData::new);
    }

    @Override
    public void registerMethodExit() {
        final OutputData methodData = outputData.get();
        int currentDepth = methodData.depth();
        methodData.withDepth(--currentDepth);
        if (currentDepth == -1) {
            methodData.withTraceId(null);
            methodData.withDepth(0);
        }
    }

    @Override
    protected boolean isRegistrationOccurredOnTrace() {
        return outputData.get().traceId() != null;
    }

    @Override
    public void registerMethodEnter(final String method) {
        final OutputData methodData = outputData.get();
        if (methodData.traceId() == null) {
            methodData.withTraceId(generateTraceId(method));
        } else {
            methodData.withDepth(methodData.depth() + 1);
        }
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
            registerMethodExit();
        }
    }

    @Override
    protected void write(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime
    ) {
        final OutputData output = this.outputData.get();
        output.fill(
                method,
                methodElapsedTime,
                methodEnterTimestamp,
                output.traceId(),
                output.depth()
        );

        this.outputSink.write(output);
    }

    private String generateTraceId(final String method) {
        final UUID uuid = UUID.randomUUID();
        final long uuidLsb = uuid.getLeastSignificantBits();
        final long result = (uuidLsb ^ method.hashCode()) * 3141592653589793L + System.nanoTime();
        return uuid + "-" + Long.toHexString(result);
    }
}
