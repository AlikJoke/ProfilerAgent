package ru.joke.profiler.configuration.meta;

import ru.joke.profiler.configuration.InvalidConfigurationException;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.joke.profiler.configuration.meta.ReflectionUtil.findAnnotatedConstructor;

public interface ConfigurationParser<T> {

    T parse(
            AnnotatedElement annotatedElement,
            ProfilerConfigurationPropertiesWrapper configuration,
            Map<String, String> properties
    );

    static <T> T parse(
            Class<T> configurationType,
            Map<String, String> properties
    ) {
        CacheableParsersFactory.init();
        try {
            final Optional<Constructor<T>> constructor = findAnnotatedConstructor(configurationType, ProfilerConfigurationPropertiesWrapper.class);
            return constructor
                    .map(c -> parse(configurationType, c, properties))
                    .orElseThrow(() -> new InvalidConfigurationException(String.format("No any annotated by %s constructor found on class %s", ProfilerConfigurationPropertiesWrapper.class.getCanonicalName(), configurationType.getCanonicalName())));
        } finally {
            CacheableParsersFactory.clear();
        }
    }

    static <T> T parse(
            @SuppressWarnings("unused") Class<T> tokenType,
            AnnotatedElement annotatedElement,
            Map<String, String> properties
    ) {
        final ProfilerConfigurationPropertiesWrapper wrapper = annotatedElement.getAnnotation(ProfilerConfigurationPropertiesWrapper.class);
        if (wrapper == null) {
            throw new InvalidConfigurationException(String.format("Provided not annotated by %s element", ProfilerConfigurationPropertiesWrapper.class.getCanonicalName()));
        }

        final String propertiesPrefix = wrapper.prefix();
        @SuppressWarnings("unchecked")
        final ConfigurationParser<T> parser = CacheableParsersFactory.create(wrapper.parser());
        final Map<String, String> configurationProperties =
                propertiesPrefix.isEmpty()
                        ? properties
                        : properties.entrySet()
                                    .stream()
                                    .filter(e -> e.getKey().startsWith(propertiesPrefix))
                                    .collect(Collectors.toMap(e -> e.getKey().substring(propertiesPrefix.length()), Map.Entry::getValue, (v1, v2) -> v1));
        return parser.parse(annotatedElement, wrapper, configurationProperties);
    }

    abstract class CacheableParsersFactory {

        private static ThreadLocal<Map<Class<?>, Object>> parsersThreadCache;

        static <T> T create(final Class<T> parserType) {
            if (parsersThreadCache == null || !parserType.isAnnotationPresent(StatelessParser.class)) {
                return ReflectionUtil.createInstance(parserType);
            }

            @SuppressWarnings("unchecked")
            final T parser = (T) parsersThreadCache.get().computeIfAbsent(parserType, ReflectionUtil::createInstance);
            return parser;
        }

        private static void init() {
            parsersThreadCache = ThreadLocal.withInitial(HashMap::new);
        }

        private static void clear() {
            parsersThreadCache.remove();
            parsersThreadCache = null;
        }

        private CacheableParsersFactory() {}
    }
}
