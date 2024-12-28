package ru.joke.profiler.output;

import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.OutputDataSink;

public final class SimpleExecutionTimeRegistrar extends ExecutionTimeRegistrar {

    private final ThreadLocal<OutputData> outputData;
    private final OutputDataSink<OutputData> outputSink;

    public SimpleExecutionTimeRegistrar(final OutputDataSink<OutputData> outputSink) {
        this.outputSink = outputSink;
        this.outputData = ThreadLocal.withInitial(OutputData::new);
    }

    @Override
    protected void write(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime
    ) {
        final OutputData output = this.outputData.get();
        output.fill(method, methodElapsedTime, methodEnterTimestamp);

        this.outputSink.write(output);
    }
}
