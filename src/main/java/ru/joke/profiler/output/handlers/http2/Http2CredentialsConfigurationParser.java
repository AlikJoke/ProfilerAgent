package ru.joke.profiler.output.handlers.http2;

import ru.joke.profiler.configuration.meta.ConfigurationParser;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.StatelessParser;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;

@StatelessParser
final class Http2CredentialsConfigurationParser implements ConfigurationParser<Http2SinkConfiguration.OutputEndpointConfiguration.AuthenticationConfiguration.Credentials> {

    @Override
    public Http2SinkConfiguration.OutputEndpointConfiguration.AuthenticationConfiguration.Credentials parse(
            final Class<Http2SinkConfiguration.OutputEndpointConfiguration.AuthenticationConfiguration.Credentials> type,
            final AnnotatedElement annotatedElement,
            final ProfilerConfigurationPropertiesWrapper configuration,
            final Map<String, String> properties
    ) {
        final String providerPropertyName = configuration.conditionalOn();
        final String providerPropertyValueStr = properties.get(providerPropertyName);
        if (providerPropertyValueStr == null || providerPropertyValueStr.isEmpty()) {
            return null;
        }

        final Http2SinkConfiguration.OutputEndpointConfiguration.AuthenticationConfiguration.AuthProvider provider = Http2SinkConfiguration.OutputEndpointConfiguration.AuthenticationConfiguration.AuthProvider.valueOf(providerPropertyValueStr.toUpperCase());
        if (provider.credentialsType() == null) {
            return null;
        }

        return ConfigurationParser.parse(provider.credentialsType(), properties);
    }
}
