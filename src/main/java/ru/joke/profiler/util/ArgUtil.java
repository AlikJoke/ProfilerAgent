package ru.joke.profiler.util;

import ru.joke.profiler.ProfilerException;

public class ArgUtil {

    public static <T> T checkNotNull(final T obj, final String arg) {
        if (obj == null) {
            throw new ProfilerException("Argument " + arg + " must be not null");
        }

        return obj;
    }

    public static String checkNotEmpty(final String str, final String arg) {
        if (str == null || str.isEmpty()) {
            throw new ProfilerException("Argument " + arg + " must be not empty");
        }

        return str;
    }

    public static int checkPositive(final int value, final String arg) {
        if (value <= 0) {
            throw new ProfilerException("Argument " + arg + "must be positive");
        }

        return value;
    }

    public static long checkPositive(final long value, final String arg) {
        if (value <= 0) {
            throw new ProfilerException("Argument " + arg + "must be positive");
        }

        return value;
    }

    public static int checkNonNegative(final int value, final String arg) {
        if (value < 0) {
            throw new ProfilerException("Argument " + arg + "must be non negative");
        }

        return value;
    }

    public static long checkNonNegative(final long value, final String arg) {
        if (value < 0) {
            throw new ProfilerException("Argument " + arg + "must be non negative");
        }

        return value;
    }
}
