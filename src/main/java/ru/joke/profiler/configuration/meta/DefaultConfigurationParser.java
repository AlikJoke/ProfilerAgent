package ru.joke.profiler.configuration.meta;

import ru.joke.profiler.configuration.InvalidConfigurationException;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Map;

import static ru.joke.profiler.configuration.meta.ReflectionUtil.createInstance;
import static ru.joke.profiler.configuration.meta.ReflectionUtil.findAnnotatedConstructor;

@StatelessParser
public final class DefaultConfigurationParser<T> implements ConfigurationParser<T> {

    @Override
    public T parse(
            final Class<T> type,
            final AnnotatedElement annotatedElement,
            final ProfilerConfigurationPropertiesWrapper configuration,
            final Map<String, String> properties
    ) {
        final Constructor<T> targetConstructor = findConstructor(type, annotatedElement);
        final Object[] args = new Object[targetConstructor.getParameterCount()];
        for (int i = 0; i < targetConstructor.getParameterCount(); i++) {
            final Parameter parameter = targetConstructor.getParameters()[i];
            args[i] = createPropertyInstance(parameter, properties);
        }

        return createInstance(targetConstructor, args);
    }

    private Constructor<T> findConstructor(
            final Class<T> type,
            final AnnotatedElement annotatedElement
    ) {
        if (!(annotatedElement instanceof Constructor)) {
            return findAnnotatedConstructor(type, ProfilerConfigurationPropertiesWrapper.class)
                    .orElseThrow(() -> new InvalidConfigurationException("Supported only constructor as annotated element in this implementation"));
        } else {
            @SuppressWarnings("unchecked")
            final Constructor<T> annotatedConstructor = (Constructor<T>) annotatedElement;
            return annotatedConstructor;
        }
    }

    private Object createPropertyInstance(
            final Parameter parameter,
            final Map<String, String> properties
    ) {
        @SuppressWarnings("unchecked")
        final Class<Object> parameterType = (Class<Object>) parameter.getType();

        if (parameter.isAnnotationPresent(ProfilerConfigurationPropertiesWrapper.class)) {
            return ConfigurationParser.parse(parameterType, parameter, properties);
        }

        final ProfilerConfigurationProperty property = parameter.getAnnotation(ProfilerConfigurationProperty.class);
        if (property == null) {
            return findAnnotatedConstructor(parameterType, ProfilerConfigurationPropertiesWrapper.class)
                        .map(c -> ConfigurationParser.parse(parameterType, c, properties))
                        .orElse(null);
        }

        @SuppressWarnings("unchecked")
        final ConfigurationPropertyParser<Object> propertyParser = CacheableParsersFactory.create(property.parser());
        final String propertyValue = properties.getOrDefault(property.name(), property.defaultValue());

        return propertyParser.parse(property, parameterType, propertyValue);
    }
}
