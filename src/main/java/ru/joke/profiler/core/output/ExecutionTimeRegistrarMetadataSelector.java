package ru.joke.profiler.core.output;

import ru.joke.profiler.core.ProfilerException;

import java.lang.reflect.Method;

public final class ExecutionTimeRegistrarMetadataSelector {

    private static final String ENTER_METHOD_NAME = "registerMethodEnter";
    private static final String EXIT_METHOD_NAME = "registerMethodExit";
    private static final String REGISTRAR_ACCESSOR_METHOD_NAME = "getInstance";

    private static final String TIME_REGISTRATION_METHOD_SIGNATURE = "(Ljava/lang/String;JJ)V";
    private static final String ENTER_EXIT_METHOD_SIGNATURE = "()V";

    private final String registrarClass;
    private final String registrarAccessorMethodSignature;
    private final String timeRegistrationMethodName;
    private final String enterMethodRegistrationName;
    private final String exitMethodRegistrationName;

    public ExecutionTimeRegistrarMetadataSelector() {
        this.registrarClass = ExecutionTimeRegistrar.class.getCanonicalName().replace('.', '/');
        this.registrarAccessorMethodSignature = buildRegistrarAccessorMethodSignature(this.registrarClass);
        this.timeRegistrationMethodName = findTimeRegistrationMethod();
        this.enterMethodRegistrationName = findEnterRegistrationMethodName();
        this.exitMethodRegistrationName = findExitRegistrationMethodName();
    }

    public String selectEnterRegistrationMethod() {
        return this.enterMethodRegistrationName;
    }

    public String selectExitRegistrationMethod() {
        return this.exitMethodRegistrationName;
    }

    public String selectRegistrarSingletonAccessorMethod() {
        return REGISTRAR_ACCESSOR_METHOD_NAME;
    }

    public String selectRegistrarSingletonAccessorSignature() {
        return this.registrarAccessorMethodSignature;
    }

    public String selectRegistrarClass() {
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

    private String buildRegistrarAccessorMethodSignature(final String registrarClass) {
        return "()L" + registrarClass + ";";
    }

    private String findTimeRegistrationMethod() {
        try {
            final Method result = ExecutionTimeRegistrar.class.getMethod(EXIT_METHOD_NAME, String.class, long.class, long.class);
            return result.getName();
        } catch (NoSuchMethodException e) {
            throw new ProfilerException(e);
        }
    }

    private String findEnterRegistrationMethodName() {
        return findVisitRegistrationMethodName(ENTER_METHOD_NAME);
    }

    private String findExitRegistrationMethodName() {
        return findVisitRegistrationMethodName(EXIT_METHOD_NAME);
    }

    private String findVisitRegistrationMethodName(final String methodName) {

        try {
            final Method result = ExecutionTimeRegistrar.class.getMethod(methodName);
            return result.getName();
        } catch (NoSuchMethodException e) {
            throw new ProfilerException(e);
        }
    }
}
