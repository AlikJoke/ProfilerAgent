package ru.joke.profiler.core.transformation;

import ru.joke.profiler.core.configuration.StaticProfilingConfiguration;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class TransformationFilter implements Predicate<String> {

    private final StaticProfilingConfiguration configuration;
    private final Set<String> loadedClasses;

    public TransformationFilter(final StaticProfilingConfiguration configuration, final Instrumentation instrumentation) {
        this.configuration = configuration;
        this.loadedClasses =
                Arrays.stream(instrumentation.getAllLoadedClasses())
                        .map(Class::getCanonicalName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
    }

    @Override
    public boolean test(final String className) {
        return !isClassFromSystemPackage(className)
                && !isClassFromAgentLibraryPackage(className)
                && !isClassFromShadedDependencies(className)
                && !isClassAlreadyLoaded(className)
                && this.configuration.isResourceMustBeProfiled(className);
    }

    private boolean isClassAlreadyLoaded(final String className) {
        return this.loadedClasses.contains(className);
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
