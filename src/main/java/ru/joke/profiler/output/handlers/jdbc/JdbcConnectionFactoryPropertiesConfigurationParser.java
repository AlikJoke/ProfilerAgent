package ru.joke.profiler.output.handlers.jdbc;

import ru.joke.profiler.configuration.meta.ConfigurationParser;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.StatelessParser;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.Properties;

import static ru.joke.profiler.configuration.ConfigurationProperties.STATIC_JDBC_SINK_CONNECTION_FACTORY_URL;

@StatelessParser
final class JdbcConnectionFactoryPropertiesConfigurationParser implements ConfigurationParser<Properties> {

    @Override
    public Properties parse(
            final AnnotatedElement annotatedElement,
            final ProfilerConfigurationPropertiesWrapper configuration,
            final Map<String, String> properties
    ) {
        final Properties connectionProperties = new Properties();
        properties.forEach((p, v) -> {
            if (!p.equals(STATIC_JDBC_SINK_CONNECTION_FACTORY_URL)) {
                connectionProperties.put(p, v);
            }
        });

        return connectionProperties;
    }
}
