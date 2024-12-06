package ru.joke.profiler.core.output;

import ru.joke.profiler.core.ProfilerException;
import ru.joke.profiler.core.output.meta.MethodEnterHandle;
import ru.joke.profiler.core.output.meta.MethodExitHandle;
import ru.joke.profiler.core.output.meta.MethodInstanceAccessorHandle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        this.registrarClass = transformClassName(registrarClass);

        final Method accessorMethod = findAnnotatedMethod(registrarClass, MethodInstanceAccessorHandle.class, a -> true);
        this.registrarAccessorMethodName = accessorMethod.getName();
        this.registrarAccessorMethodSignature = buildMethodSignature(accessorMethod);

        final Method enterMethod = findAnnotatedMethod(registrarClass, MethodEnterHandle.class, a -> true);
        this.enterMethodRegistrationName = enterMethod.getName();
        this.enterMethodRegistrationSignature = buildMethodSignature(enterMethod);

        final Method exitTimeRegistrationMethod = findAnnotatedMethod(registrarClass, MethodExitHandle.class, MethodExitHandle::forTimeRegistration);
        this.exitMethodTimeRegistrationName = exitTimeRegistrationMethod.getName();
        this.exitMethodTimeRegistrationSignature = buildMethodSignature(exitTimeRegistrationMethod);

        final Method exitMethod = findAnnotatedMethod(registrarClass, MethodExitHandle.class, a -> !a.forTimeRegistration());
        this.exitMethodRegistrationName = exitMethod.getName();
        this.exitMethodRegistrationSignature = buildMethodSignature(exitMethod);
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

    private String buildMethodSignature(final Method method) {
        final Class<?> returnType = method.getReturnType();
        final String returnTypeSignature = getTypeSignature(returnType);

        final Class<?>[] parameters = method.getParameterTypes();
        final String paramsSignature =
                Arrays.stream(parameters)
                        .map(this::getTypeSignature)
                        .collect(Collectors.joining("", "(", ")"));
        return paramsSignature + returnTypeSignature;
    }

    private String getTypeSignature(final Class<?> type) {
        if (type == void.class) {
            return "V";
        } else if (type == long.class) {
            return "J";
        } else if (type == int.class) {
            return "I";
        } else if (type == boolean.class) {
            return "Z";
        } else if (type == char.class) {
            return "C";
        } else if (type == short.class) {
            return "S";
        } else if (type == byte.class) {
            return "B";
        } else if (type == float.class) {
            return "F";
        } else if (type == double.class) {
            return "D";
        } else {
            return "L" + transformClassName(type) + ";";
        }
    }

    private <T extends Annotation> Method findAnnotatedMethod(
            final Class<?> registrarClass,
            final Class<T> annotation,
            final Predicate<T> annotationFilter) {
        final Method[] methods = registrarClass.getMethods();
        return Arrays.stream(methods)
                        .filter(m -> m.isAnnotationPresent(annotation) && annotationFilter.test(m.getAnnotation(annotation)))
                        .findAny()
                        .orElseThrow(() -> new ProfilerException(String.format("No annotated %s method found in registrar class", annotation.getCanonicalName())));
    }

    private String transformClassName(final Class<?> clazz) {
        return clazz.getCanonicalName().replace('.', '/');
    }
}
