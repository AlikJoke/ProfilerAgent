package ru.joke.profiler.output.sinks.http2;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.impl.auth.CredentialsProviderBuilder;
import org.apache.hc.client5.http.impl.auth.SystemDefaultCredentialsProvider;
import org.apache.hc.core5.http.HttpHost;

final class Http2ClientCredentialsProviderFactory {

    private final Http2SinkConfiguration.OutputEndpointConfiguration configuration;

    Http2ClientCredentialsProviderFactory(final Http2SinkConfiguration.OutputEndpointConfiguration configuration) {
        this.configuration = configuration;
    }

    CredentialsProvider create() {
        final Http2SinkConfiguration.OutputEndpointConfiguration.AuthenticationConfiguration authConfiguration = configuration.authenticationConfiguration();
        switch (authConfiguration.provider()) {
            case SYSTEM_DEFAULT:
                return new SystemDefaultCredentialsProvider();
            case BASIC:
                final HttpHost host = new HttpHost(configuration.outputScheme(), configuration.outputHost(), configuration.outputPort());
                final AuthScope scope = new AuthScope(host, authConfiguration.realm(), null);
                final Http2SinkConfiguration.OutputEndpointConfiguration.AuthenticationConfiguration.BasicCredentials credentials = (Http2SinkConfiguration.OutputEndpointConfiguration.AuthenticationConfiguration.BasicCredentials) authConfiguration.credentials();
                return CredentialsProviderBuilder
                        .create()
                            .add(scope, credentials.username(), credentials.password())
                        .build();
            default:
                return null;
        }
    }
}
