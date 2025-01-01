package ru.joke.profiler.output;

import ru.joke.profiler.ProfilerException;
import ru.joke.profiler.output.meta.MethodEnterHandle;
import ru.joke.profiler.output.meta.MethodExitHandle;
import ru.joke.profiler.output.meta.MethodInstanceAccessorHandle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Predicate;

import static ru.joke.profiler.util.BytecodeUtil.buildMethodDescriptor;
import static ru.joke.profiler.util.BytecodeUtil.toBytecodeFormat;

public final class ExecutionTimeRegistrarMetadataSelector {

    private final String registrarClass;
    private final String registrarAccessorMethodName;
    private final String registrarAccessorMethodSignature;
    private final String enterMethodRegistrationName;
    private final String enterMethodRegistrationSignature;
    private final String exitMethodRegistrationName;
    private final String exitMethodRegistrationSignature;
    private final String exitMethodTimeRegistrationName;
    private final String exitMethodTimeRegistrationSignature;

    public ExecutionTimeRegistrarMetadataSelector(final Class<?> registrarClass) {
        this.registrarClass = toBytecodeFormat(registrarClass);

        final Method accessorMethod = findAnnotatedMethod(registrarClass, MethodInstanceAccessorHandle.class, a -> true);
        this.registrarAccessorMethodName = accessorMethod.getName();
        this.registrarAccessorMethodSignature = buildMethodDescriptor(accessorMethod);

        final Method enterMethod = findAnnotatedMethod(registrarClass, MethodEnterHandle.class, a -> true);
        this.enterMethodRegistrationName = enterMethod.getName();
        this.enterMethodRegistrationSignature = buildMethodDescriptor(enterMethod);

        final Method exitTimeRegistrationMethod = findAnnotatedMethod(registrarClass, MethodExitHandle.class, MethodExitHandle::forTimeRegistration);
        this.exitMethodTimeRegistrationName = exitTimeRegistrationMethod.getName();
        this.exitMethodTimeRegistrationSignature = buildMethodDescriptor(exitTimeRegistrationMethod);

        final Method exitMethod = findAnnotatedMethod(registrarClass, MethodExitHandle.class, a -> !a.forTimeRegistration());
        this.exitMethodRegistrationName = exitMethod.getName();
        this.exitMethodRegistrationSignature = buildMethodDescriptor(exitMethod);
    }

    public String selectEnterRegistrationMethod() {
        return this.enterMethodRegistrationName;
    }

    public String selectExitRegistrationMethod() {
        return this.exitMethodRegistrationName;
    }

    public String selectRegistrarSingletonAccessorMethod() {
        return this.registrarAccessorMethodName;
    }

    public String selectRegistrarSingletonAccessorSignature() {
        return this.registrarAccessorMethodSignature;
    }

    public String selectRegistrarClass() {
        return this.registrarClass;
    }

    public String selectExitTimeRegistrationMethod() {
        return this.exitMethodTimeRegistrationName;
    }

    public String selectTimeRegistrationMethodSignature() {
        return this.exitMethodTimeRegistrationSignature;
    }

    public String selectEnterRegistrationMethodSignature() {
        return this.enterMethodRegistrationSignature;
    }

    public String selectExitRegistrationMethodSignature() {
        return this.exitMethodRegistrationSignature;
    }

    private <T extends Annotation> Method findAnnotatedMethod(
            final Class<?> registrarClass,
            final Class<T> annotation,
            final Predicate<T> annotationFilter
    ) {
        final Method[] methods = registrarClass.getMethods();
        return Arrays.stream(methods)
                        .filter(m -> m.isAnnotationPresent(annotation) && annotationFilter.test(m.getAnnotation(annotation)))
                        .findAny()
                        .orElseThrow(() -> new ProfilerException(String.format("No annotated %s method found in registrar class", annotation.getCanonicalName())));
    }
}
