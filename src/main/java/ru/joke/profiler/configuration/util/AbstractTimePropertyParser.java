package ru.joke.profiler.configuration.util;

import ru.joke.profiler.configuration.InvalidConfigurationException;
import ru.joke.profiler.configuration.meta.ConfigurationPropertyParser;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class AbstractTimePropertyParser implements ConfigurationPropertyParser<Long> {

    private static final String NO_VALUE = "-1";
    private static final Pattern pattern = Pattern.compile("^(\\d+)(ms|ns|mcs|m|s)?$");

    private final ProfilingTimeUnit defaultTimeUnit;

    AbstractTimePropertyParser(final ProfilingTimeUnit defaultTimeUnit) {
        this.defaultTimeUnit = defaultTimeUnit;
    }

    @Override
    public Long parse(
            final ProfilerConfigurationProperty property,
            final Class<Long> propertyType,
            final String propertyValue) {
        if (property.required() && propertyValue.isEmpty()) {
            throw new InvalidConfigurationException(String.format("Property (%s) is required", property.name()));
        }

        if (propertyValue.equals(NO_VALUE)) {
            return Long.valueOf(NO_VALUE);
        }

        final Matcher matcher = pattern.matcher(propertyValue);
        if (!matcher.find()) {
            throw new InvalidConfigurationException(String.format("Invalid format of property %s: %s", property.name(), propertyValue));
        }

        final String timeValueStr = matcher.group(1);
        final long timeValue = Long.parseLong(timeValueStr);

        final String timeUnitStr = matcher.groupCount() > 1 ? matcher.group(2) : null;
        final ProfilingTimeUnit timeUnit = ProfilingTimeUnit.parse(timeUnitStr, this.defaultTimeUnit);

        return this.defaultTimeUnit.toJavaTimeUnit().convert(timeValue, timeUnit.toJavaTimeUnit());
    }
}
