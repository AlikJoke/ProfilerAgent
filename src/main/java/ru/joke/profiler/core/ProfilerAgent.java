package ru.joke.profiler.core;

import ru.joke.profiler.core.configuration.StaticProfilingConfiguration;
import ru.joke.profiler.core.transformation.ProfilingTransformer;
import ru.joke.profiler.core.transformation.TransformationFilter;

import java.lang.instrument.Instrumentation;
import java.util.function.Predicate;

public final class ProfilerAgent {

    public static void premain(final String args, final Instrumentation instrumentation) {
        final StaticProfilingConfiguration configuration = StaticProfilingConfiguration.parse(args);
        final Predicate<String> transformationFilter = new TransformationFilter(configuration);

        instrumentation.addTransformer(new ProfilingTransformer(transformationFilter, configuration));
    }
}
