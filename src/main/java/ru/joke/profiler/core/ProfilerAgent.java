package ru.joke.profiler.core;

import java.lang.instrument.Instrumentation;

public final class ProfilerAgent {

    public static void premain(final String args, final Instrumentation instrumentation) {
        final ProfilingConfiguration configuration = new ProfilingConfiguration(args);
        instrumentation.addTransformer(new ProfilingTransformer(configuration));
    }
}
