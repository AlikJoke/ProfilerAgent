package ru.joke.profiler.util;

import ru.joke.profiler.ProfilerException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import static ru.joke.profiler.util.ArgUtil.checkNotEmpty;
import static ru.joke.profiler.util.ArgUtil.checkNotNull;

public abstract class BytecodeUtil {

    public static final String CONSTRUCTOR_NAME = "<init>";
    public static final String ARRAY = "[";
    public static final String OBJECT_TYPE = toBytecodeFormat(Object.class);

    public static boolean isArrayType(final String type) {
        return checkNotNull(type, "type").startsWith(ARRAY);
    }

    public static String getTargetArrayType(final String arrayType) {
        return checkNotEmpty(arrayType, "arrayType").substring(1);
    }

    public static String toCanonicalFormat(final String bytecodeFormat) {
        return checkNotNull(bytecodeFormat, "bytecodeFormat").replace('/', '.');
    }

    public static String insertObjectParameterInTheBeginningOfMethodDescriptor(
            final String original,
            final String parameterType
    ) {
        return '(' + createObjectTypeDescriptor(checkNotEmpty(parameterType, "parameterType")) + checkNotEmpty(original.substring(1), "original");
    }

    public static String buildMethodDescriptor(final Class<?> owner, final String methodName) {
        return Arrays.stream(checkNotNull(owner, "owner").getMethods())
                        .filter(m -> m.getName().equals(methodName))
                        .findAny()
                        .map(BytecodeUtil::buildMethodDescriptor)
                        .orElseThrow(() -> new ProfilerException(String.format("Unable to find method %s in the class %s", methodName, owner)));
    }

    public static String buildMethodDescriptor(final Method method) {
        final Class<?> returnType = checkNotNull(method, "method").getReturnType();
        final String returnTypeSignature = getTypeSignature(returnType);

        final Class<?>[] parameters = method.getParameterTypes();
        final String paramsSignature =
                Arrays.stream(parameters)
                        .map(BytecodeUtil::getTypeSignature)
                        .collect(Collectors.joining("", "(", ")"));
        return paramsSignature + returnTypeSignature;
    }

    public static String toBytecodeFormat(final Class<?> clazz) {
        return checkNotNull(clazz, "clazz").getCanonicalName().replace('.', '/');
    }

    private static String getTypeSignature(final Class<?> type) {
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
            return createObjectTypeDescriptor(toBytecodeFormat(type));
        }
    }

    private static String createObjectTypeDescriptor(final String type) {
        return "L" + type + ";";
    }

    private BytecodeUtil() {}
}
