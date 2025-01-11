package ru.joke.profiler.transformation;

import ru.joke.profiler.configuration.StaticProfilingConfiguration;

import java.util.function.Predicate;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;
import static ru.joke.profiler.util.BytecodeUtil.isArrayType;

public final class TransformationFilter implements Predicate<String> {

    private static final String JAVA_PACKAGE = "java/";
    private static final String JDK_PACKAGE = "jdk/";
    private static final String JAVAX_PACKAGE = "javax/";
    private static final String SUN_PACKAGE = "sun/";
    private static final String COM_SUN_PACKAGE = "com/sun/";
    private static final String SHADED_PACKAGE = "ru/joke_shaded/";
    private static final String PROFILER_PACKAGE = "ru/joke/profiler/";

    private final StaticProfilingConfiguration configuration;

    public TransformationFilter(final StaticProfilingConfiguration configuration) {
        this.configuration = checkNotNull(configuration, "configuration");
    }

    @Override
    public boolean test(final String className) {
        return className != null
                && !isClassFromSystemPackage(className)
                && !isClassFromAgentLibraryPackage(className)
                && !isClassFromShadedDependencies(className)
                && !isArrayType(className)
                && this.configuration.isResourceMustBeProfiled(className);
    }

    private boolean isClassFromSystemPackage(final String className) {
        return className.startsWith(JAVA_PACKAGE)
                || className.startsWith(JDK_PACKAGE)
                || className.startsWith(JAVAX_PACKAGE)
                || className.startsWith(SUN_PACKAGE)
                || className.startsWith(COM_SUN_PACKAGE);
    }

    private boolean isClassFromShadedDependencies(final String className) {
        return className.startsWith(SHADED_PACKAGE);
    }

    private boolean isClassFromAgentLibraryPackage(final String className) {
        return className.startsWith(PROFILER_PACKAGE);
    }
}
