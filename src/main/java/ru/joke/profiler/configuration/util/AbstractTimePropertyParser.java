package ru.joke.profiler.configuration.util;

import ru.joke.profiler.configuration.InvalidConfigurationException;
import ru.joke.profiler.configuration.meta.ConfigurationPropertyParser;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class AbstractTimePropertyParser implements ConfigurationPropertyParser<Long> {

    private static final Pattern pattern = Pattern.compile("^([0-9]+)(ns|mcs|ms|s|m|h)*$");

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

        final Matcher matcher = pattern.matcher(propertyValue);
        if (!matcher.matches()) {
            throw new InvalidConfigurationException(String.format("Invalid format of property %s: %s", property.name(), propertyValue));
        }

        final String timeValueStr = matcher.group(0);
        final long timeValue = Long.parseLong(timeValueStr);

        final String timeUnitStr = matcher.groupCount() > 1 ? matcher.group(1) : null;
        final ProfilingTimeUnit timeUnit = ProfilingTimeUnit.parse(timeUnitStr, this.defaultTimeUnit);

        return this.defaultTimeUnit.toJavaTimeUnit().convert(timeValue, timeUnit.toJavaTimeUnit());
    }
}
