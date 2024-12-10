package ru.joke.profiler.core.output;

import ru.joke.profiler.core.output.handlers.OutputData;
import ru.joke.profiler.core.output.handlers.OutputDataSink;

public final class SimpleExecutionTimeRegistrar extends ExecutionTimeRegistrar {

    private final ThreadLocal<OutputData> outputData;
    private final OutputDataSink outputSink;

    public SimpleExecutionTimeRegistrar(final OutputDataSink outputSink) {
        this.outputSink = outputSink;
        this.outputData = ThreadLocal.withInitial(OutputData::new);
    }

    @Override
    protected void write(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime) {
        final OutputData output = this.outputData.get();
        output.fill(method, methodElapsedTime, methodEnterTimestamp, null, 0);

        this.outputSink.write(output);
    }
}
