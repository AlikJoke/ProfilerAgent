package ru.joke.profiler.core.output;

public final class SimpleExecutionTimeRegistrar extends ExecutionTimeRegistrar {

    @Override
    protected void write(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime) {
        // TODO
        System.out.println(method + ":" + methodEnterTimestamp + ":" + methodElapsedTime);
    }
}
