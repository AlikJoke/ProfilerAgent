package ru.joke.profiler.configuration.meta;

import ru.joke.profiler.configuration.InvalidConfigurationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

abstract class ReflectionUtil {

    static <T> Optional<Constructor<T>> findAnnotatedConstructor(
            final Class<T> type,
            final Class<? extends Annotation> annotationType
    ) {
        return Arrays.stream(type.getDeclaredConstructors())
                        .filter(c -> c.isAnnotationPresent(annotationType))
                        .findAny()
                        .map(ReflectionUtil::cast);
    }

    static Field findField(
            final Class<?> type,
            final String fieldName
    ) {
        try {
            return type.getField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new InvalidConfigurationException(e);
        }
    }

    static <T> T createInstance(final Class<T> type) {
        try {
            final Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new InvalidConfigurationException(e);
        }
    }

    static <T> T createInstance(
            final Constructor<T> constructor,
            final Object[] args
    ) {
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new InvalidConfigurationException(e);
        }
    }

    private static <T> Constructor<T> cast(final Constructor<?> constructor) {
        @SuppressWarnings("unchecked")
        final Constructor<T> result = (Constructor<T>) constructor;
        return result;
    }

    private ReflectionUtil() {}
}
