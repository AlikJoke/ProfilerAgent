package ru.joke.profiler.configuration.meta;

import ru.joke.profiler.configuration.InvalidConfigurationException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static ru.joke.profiler.configuration.meta.ReflectionUtil.findField;

@StatelessParser
public class DefaultConfigurationPropertyParser implements ConfigurationPropertyParser<Object> {

    private final Map<Class<?>, Function<String, ?>> convertors;

    public DefaultConfigurationPropertyParser() {
        final Map<Class<?>, Function<String, ?>> convertors = new HashMap<>(8, 1);
        convertors.put(int.class, Integer::valueOf);
        convertors.put(Integer.class, Integer::valueOf);
        convertors.put(long.class, Long::valueOf);
        convertors.put(Long.class, Long::valueOf);
        convertors.put(boolean.class, Boolean::valueOf);
        convertors.put(Boolean.class, Boolean::valueOf);
        convertors.put(String.class, s -> s.isEmpty() ? null : s);
        convertors.put(char[].class, String::toCharArray);

        this.convertors = Collections.unmodifiableMap(convertors);
    }

    @Override
    public Object parse(
            final ProfilerConfigurationProperty property,
            final Class<Object> propertyType,
            final String propertyValue
    ) {
        if (propertyType.isEnum()) {
            return parseEnum(propertyValue, propertyType, property.required());
        }

        if (property.required() && propertyValue.isEmpty()) {
            throw new InvalidConfigurationException(String.format("Property (%s) is required", property.name()));
        }

        final Function<String, ?> convertor = this.convertors.get(propertyType);
        if (convertor == null) {
            throw new InvalidConfigurationException(String.format("Unsupported type of property %s: %s", property.name(), propertyType.getCanonicalName()));
        }

        return convertor.apply(propertyValue);
    }

    private <T> T parseEnum(
            final String value,
            final Class<T> enumType,
            final boolean required
    ) {
        for (final T enumElem : enumType.getEnumConstants()) {
            final String enumName = enumElem.toString();
            final String enumId;
            if (enumElem instanceof AliasedEnumElement) {
                enumId = ((AliasedEnumElement) enumElem).alias();
            } else {
                enumId = enumName;
            }

            if (value.equalsIgnoreCase(enumId)
                    || value.isEmpty() && findField(enumType, enumName).isAnnotationPresent(ProfilerDefaultEnumProperty.class)) {
                return enumElem;
            }
        }

        if (!required) {
            return null;
        }

        throw new InvalidConfigurationException(String.format("Unknown element %s in enum %s", value, enumType.getCanonicalName()));
    }
}
