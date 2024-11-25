package ru.joke.profiler.core;

public class ExecutionTimeRegistrar {

    public static void register(final String method, final long methodEnterTimestamp, final long methodExitTimestamp) {
        System.out.println(method + ":" + methodEnterTimestamp + ":" + (methodExitTimestamp - methodEnterTimestamp));
    }
}
