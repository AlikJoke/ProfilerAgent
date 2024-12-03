package ru.joke.profiler.core.output;

import ru.joke.profiler.core.ProfilerException;
import ru.joke.profiler.core.configuration.StaticProfilingConfiguration;

import java.lang.reflect.Method;

public final class ExecutionTimeRegistrarMetadataSelector {

    private static final String DYNAMIC_METHOD_NAME = "registerDynamic";
    private static final String STATIC_METHOD_NAME = "registerStatic";
    private static final String ENTER_METHOD_NAME = "registerMethodEnter";
    private static final String EXIT_METHOD_NAME = "registerMethodExit";

    private static final String TIME_REGISTRATION_METHOD_SIGNATURE = "(Ljava/lang/String;JJ)V";
    private static final String ENTER_EXIT_METHOD_SIGNATURE = "()V";

    private final Class<?> registrarClass;
    private final String timeRegistrationMethodName;
    private final String enterMethodRegistrationName;
    private final String exitMethodRegistrationName;

    public ExecutionTimeRegistrarMetadataSelector(final StaticProfilingConfiguration configuration) {
        this.registrarClass = configuration.isExecutionTracingEnabled() ? TracedExecutionTimeRegistrar.class : SimpleExecutionTimeRegistrar.class;
        this.timeRegistrationMethodName = findTimeRegistrationMethod(configuration, this.registrarClass);
        this.enterMethodRegistrationName = findEnterRegistrationMethodName(configuration, this.registrarClass);
        this.exitMethodRegistrationName = findExitRegistrationMethodName(configuration, this.registrarClass);
    }

    public String selectEnterRegistrationMethod() {
        return this.enterMethodRegistrationName;
    }

    public String selectExitRegistrationMethod() {
        return this.exitMethodRegistrationName;
    }

    public Class<?> selectRegistrarClass() {
        return this.registrarClass;
    }

    public String selectRegistrationMethod() {
        return this.timeRegistrationMethodName;
    }

    public String selectTimeRegistrationMethodSignature() {
        return TIME_REGISTRATION_METHOD_SIGNATURE;
    }

    public String selectEnterRegistrationMethodSignature() {
        return ENTER_EXIT_METHOD_SIGNATURE;
    }

    public String selectExitRegistrationMethodSignature() {
        return ENTER_EXIT_METHOD_SIGNATURE;
    }

    private String findTimeRegistrationMethod(
            final StaticProfilingConfiguration configuration,
            final Class<?> registrarClass) {
        final String targetMethodName = configuration.isDynamicConfigurationEnabled() ? DYNAMIC_METHOD_NAME : STATIC_METHOD_NAME;
        try {
            final Method result = registrarClass.getDeclaredMethod(targetMethodName, String.class, long.class, long.class);
            return result.getName();
        } catch (NoSuchMethodException e) {
            throw new ProfilerException(e);
        }
    }

    private String findEnterRegistrationMethodName(
            final StaticProfilingConfiguration configuration,
            final Class<?> registrarClass) {
        return findVisitRegistrationMethodName(configuration, registrarClass, ENTER_METHOD_NAME);
    }

    private String findExitRegistrationMethodName(
            final StaticProfilingConfiguration configuration,
            final Class<?> registrarClass) {
        return findVisitRegistrationMethodName(configuration, registrarClass, EXIT_METHOD_NAME);
    }

    private String findVisitRegistrationMethodName(
            final StaticProfilingConfiguration configuration,
            final Class<?> registrarClass,
            final String methodName) {

        if (!configuration.isExecutionTracingEnabled()) {
            return null;
        }

        try {
            final Method result = registrarClass.getDeclaredMethod(methodName);
            return result.getName();
        } catch (NoSuchMethodException e) {
            throw new ProfilerException(e);
        }
    }
}
