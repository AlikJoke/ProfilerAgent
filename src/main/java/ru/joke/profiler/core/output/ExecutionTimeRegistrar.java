package ru.joke.profiler.core.output;

public class ExecutionTimeRegistrar {

    public static void register(
            final String method,
            final long methodEnterTimestamp,
            final long methodElapsedTime) {
        // TODO
        System.out.println(method + ":" + methodEnterTimestamp + ":" + (methodElapsedTime));
    }
}
