package ru.joke.profiler.core.transformation;

import ru.joke.profiler.core.configuration.StaticProfilingConfiguration;

import java.util.function.Predicate;

public final class TransformationFilter implements Predicate<String> {

    private final StaticProfilingConfiguration configuration;

    public TransformationFilter(final StaticProfilingConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean test(final String className) {
        return !isClassFromSystemPackage(className)
                && !isClassFromAgentLibraryPackage(className)
                && !isClassFromShadedDependencies(className)
                && this.configuration.isResourceMustBeProfiled(className);
    }

    private boolean isClassFromSystemPackage(final String className) {
        return className.startsWith("java/")
                || className.startsWith("jdk/")
                || className.startsWith("javax/")
                || className.startsWith("sun/")
                || className.startsWith("com/sun/");
    }

    private boolean isClassFromShadedDependencies(final String className) {
        return className.startsWith("ru/joke_shaded/");
    }

    private boolean isClassFromAgentLibraryPackage(final String className) {
        return className.startsWith("ru/joke/profiler/");
    }
}
