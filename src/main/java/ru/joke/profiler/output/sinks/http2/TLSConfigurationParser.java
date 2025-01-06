package ru.joke.profiler.output.sinks.http2;

import ru.joke.profiler.configuration.meta.ConfigurationParser;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.StatelessParser;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;

@StatelessParser
final class TLSConfigurationParser implements ConfigurationParser<Http2SinkConfiguration.Http2ClientConfiguration.TLSConfiguration> {

    private static final String TLS_ENABLED = "use_tls";
    private static final String MUTUAL_TLS_ENABLED = "use_mutual_tls";

    @Override
    public Http2SinkConfiguration.Http2ClientConfiguration.TLSConfiguration parse(
            final Class<Http2SinkConfiguration.Http2ClientConfiguration.TLSConfiguration> type,
            final AnnotatedElement annotatedElement,
            final ProfilerConfigurationPropertiesWrapper configuration,
            final Map<String, String> properties) {
        final boolean tlsEnabled = parseBooleanProperty(properties, TLS_ENABLED);
        final boolean mutualTlsEnabled = parseBooleanProperty(properties, MUTUAL_TLS_ENABLED);
        return mutualTlsEnabled
                ? ConfigurationParser.parse(Http2SinkConfiguration.Http2ClientConfiguration.MutualTLSConfiguration.class, properties)
                : tlsEnabled
                    ? ConfigurationParser.parse(type, properties)
                    : null;
    }

    private boolean parseBooleanProperty(final Map<String, String> properties, final String property) {
        final String propertyValue = properties.get(property);
        return Boolean.parseBoolean(propertyValue);
    }
}
