package ru.joke.profiler.output.handlers.http2;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ru.joke.profiler.configuration.ConfigurationProperties.*;

final class Http2SinkConfigurationLoader {

    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    private static final String DEFAULT_SCHEME = "http";
    private static final String DEFAULT_TLS_PROTOCOL = "TLSv1.2";

    private static final int DEFAULT_PORT = -1;
    
    private static final int DEFAULT_MAX_RETRIES_ON_ERROR_SYNC = 0;
    private static final int DEFAULT_MAX_RETRIES_ON_ERROR_ASYNC = 3;

    private static final int DEFAULT_IO_THREAD_COUNT = 2;
    private static final int DEFAULT_IO_SEND_BUFFER_SIZE = 0;
    private static final int DEFAULT_IO_LINGER_MS = -1;
    private static final int DEFAULT_IO_MAX_KEEP_ALIVE_PROBES_BEFORE_DROP = 10;

    private static final int DEFAULT_REQUEST_MAX_RETRIES = 3;
    private static final int DEFAULT_REQUEST_MAX_REDIRECTS = 0;
    private static final int DEFAULT_REQUEST_INITIAL_WINDOW_SIZE = Short.MAX_VALUE * 2 + 1;
    private static final int DEFAULT_REQUEST_MAX_FRAME_SIZE = power(2, 20);

    private static final long DEFAULT_REQUEST_RETRY_INTERVAL_MS = TimeUnit.SECONDS.toMillis(5);
    private static final long DEFAULT_REQUEST_KEEP_ALIVE_INTERVAL_MS = TimeUnit.MINUTES.toMillis(3);
    private static final long DEFAULT_REQUEST_CONN_MANAGER_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(2);

    private static final long DEFAULT_CONN_VALIDATE_AFTER_INACTIVITY_MS = TimeUnit.MINUTES.toMillis(1);
    private static final long DEFAULT_CONN_IDLE_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(2);
    private static final long DEFAULT_CONN_SOCKET_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(30);
    private static final long DEFAULT_CONN_CONNECT_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(30);
    private static final long DEFAULT_CONN_TTL_MS = -1;
    
    private static final long DEFAULT_SYNC_SENDING_WAIT_MS = TimeUnit.SECONDS.toMillis(10);
    
    private static final long DEFAULT_IO_TCP_KEEP_ALIVE_PROBES_INTERVAL_MS = -1;
    private static final long DEFAULT_IO_TCP_IDLE_TIMEOUT_MS = -1;
    private static final long DEFAULT_IO_SOCKET_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(2);


    Http2SinkConfiguration load(final Map<String, String> properties) {

        final Http2SinkConfiguration.OutputMessageConfiguration outputMessageConfiguration = loadOutputMessageConfiguration(properties);
        final Http2SinkConfiguration.ProcessingConfiguration processingConfiguration = loadProcessingConfiguration(properties);
        final Http2SinkConfiguration.Http2ClientConfiguration clientConfiguration = loadHttp2ClientConfiguration(properties);

        return new Http2SinkConfiguration(
                outputMessageConfiguration,
                processingConfiguration,
                clientConfiguration
        );
    }

    private Http2SinkConfiguration.OutputMessageConfiguration loadOutputMessageConfiguration(final Map<String, String> properties) {
        final String endpoint = findRequiredProperty(properties, STATIC_HTTP2_SINK_MESSAGE_OUTPUT_ENDPOINT);
        final String host = findRequiredProperty(properties, STATIC_HTTP2_SINK_MESSAGE_OUTPUT_HOST);
        final int port = parseIntProperty(properties, STATIC_HTTP2_SINK_MESSAGE_OUTPUT_PORT, DEFAULT_PORT);
        final String scheme = properties.getOrDefault(STATIC_HTTP2_SINK_MESSAGE_OUTPUT_SCHEME, DEFAULT_SCHEME);

        final String contentType = properties.getOrDefault(STATIC_HTTP2_SINK_CONTENT_TYPE, DEFAULT_CONTENT_TYPE);

        final String propertiesMappingStr = properties.get(STATIC_HTTP2_SINK_MESSAGE_PROPERTIES_MAPPING);
        final Map<String, String> propertiesMapping = createMapping(propertiesMappingStr);

        final String headersMappingStr = properties.get(STATIC_HTTP2_SINK_MESSAGE_HEADERS_MAPPING);
        final Map<String, String> headersMapping = createMapping(headersMappingStr);

        return new Http2SinkConfiguration.OutputMessageConfiguration(
                scheme,
                host,
                port,
                endpoint,
                contentType,
                propertiesMapping,
                headersMapping
        );
    }

    private Http2SinkConfiguration.ProcessingConfiguration loadProcessingConfiguration(final Map<String, String> properties) {

        final boolean disableAsyncSending = parseBooleanProperty(properties, STATIC_HTTP2_SINK_DISABLE_ASYNC_SENDING);
        final String errorPolicy = properties.get(STATIC_HTTP2_SINK_ON_SENDING_ERROR_POLICY);
        final Http2SinkConfiguration.ProcessingConfiguration.OnErrorPolicy policy = Http2SinkConfiguration.ProcessingConfiguration.OnErrorPolicy.parse(errorPolicy);

        final int maxRetriesOnError = parseIntProperty(
                properties,
                STATIC_HTTP2_SINK_MAX_RETRIES_ON_ERROR,
                disableAsyncSending ? DEFAULT_MAX_RETRIES_ON_ERROR_SYNC : DEFAULT_MAX_RETRIES_ON_ERROR_ASYNC
        );
        final long syncSendingWaitMs = parseLongProperty(properties, STATIC_HTTP2_SINK_SYNC_SENDING_WAIT_MS, DEFAULT_SYNC_SENDING_WAIT_MS);

        return new Http2SinkConfiguration.ProcessingConfiguration(
                disableAsyncSending,
                policy,
                maxRetriesOnError,
                syncSendingWaitMs
        );
    }

    private Http2SinkConfiguration.Http2ClientConfiguration loadHttp2ClientConfiguration(final Map<String, String> properties) {

        final Http2SinkConfiguration.Http2ClientConfiguration.AuthenticationConfiguration authenticationConfiguration = loadAuthConfiguration(properties);
        final Http2SinkConfiguration.Http2ClientConfiguration.ConnectionConfiguration connectionConfiguration = loadConnectionConfiguration(properties);
        final Http2SinkConfiguration.Http2ClientConfiguration.TLSConfiguration tlsConfiguration = loadTlsConfiguration(properties);
        final Http2SinkConfiguration.Http2ClientConfiguration.RequestConfiguration requestConfiguration = loadRequestConfiguration(properties);
        final Http2SinkConfiguration.Http2ClientConfiguration.IOConfiguration ioConfiguration = loadIOConfiguration(properties);
        final boolean enableGracefulShutdown = parseBooleanProperty(properties, STATIC_HTTP2_SINK_CLIENT_GRACEFUL_SHUTDOWN);

        return new Http2SinkConfiguration.Http2ClientConfiguration(
                authenticationConfiguration,
                connectionConfiguration,
                tlsConfiguration,
                requestConfiguration,
                ioConfiguration,
                enableGracefulShutdown
        );
    }

    private Http2SinkConfiguration.Http2ClientConfiguration.IOConfiguration loadIOConfiguration(final Map<String, String> properties) {

        final int threadCount = parseIntProperty(properties, STATIC_HTTP2_SINK_IO_THREAD_COUNT, DEFAULT_IO_THREAD_COUNT);
        final int sendBufferSize = parseIntProperty(properties, STATIC_HTTP2_SINK_IO_SEND_BUFFER_SIZE, DEFAULT_IO_SEND_BUFFER_SIZE);
        final long lingerTimeoutMs = parseLongProperty(properties, STATIC_HTTP2_SINK_IO_LINGER_MS, DEFAULT_IO_LINGER_MS);
        final boolean tcpNoDelay = parseBooleanProperty(properties, STATIC_HTTP2_SINK_IO_TCP_NO_DELAY);
        final long keepAliveIntervalMs = parseLongProperty(properties, STATIC_HTTP2_SINK_IO_TCP_KEEP_ALIVE_PROBE_INTERVAL_MS, DEFAULT_IO_TCP_KEEP_ALIVE_PROBES_INTERVAL_MS);
        final long idleTimeoutMs = parseLongProperty(properties, STATIC_HTTP2_SINK_IO_TCP_IDLE_TIMEOUT_MS, DEFAULT_IO_TCP_IDLE_TIMEOUT_MS);
        final int maxKeepAliveProbesBeforeDrop = parseIntProperty(properties, STATIC_HTTP2_SINK_IO_MAX_KEEP_ALIVE_PROBES_BEFORE_DROP, DEFAULT_IO_MAX_KEEP_ALIVE_PROBES_BEFORE_DROP);
        final String socksProxyHost = properties.get(STATIC_HTTP2_SINK_IO_SOCKS_PROXY_HOST);
        final int socksProxyPort = parseIntProperty(properties, STATIC_HTTP2_SINK_IO_SOCKS_PROXY_PORT, DEFAULT_PORT);
        final String socksProxyUsername = properties.get(STATIC_HTTP2_SINK_IO_SOCKS_PROXY_USERNAME);
        final char[] socksProxyPassword = properties.getOrDefault(STATIC_HTTP2_SINK_IO_SOCKS_PROXY_PWD, "").toCharArray();
        final long ioTimeoutMs = parseLongProperty(properties, STATIC_HTTP2_SINK_IO_SOCKET_TIMEOUT_MS, DEFAULT_IO_SOCKET_TIMEOUT_MS);

        return new Http2SinkConfiguration.Http2ClientConfiguration.IOConfiguration(
                threadCount,
                sendBufferSize,
                lingerTimeoutMs,
                tcpNoDelay,
                keepAliveIntervalMs,
                idleTimeoutMs,
                maxKeepAliveProbesBeforeDrop,
                socksProxyHost,
                socksProxyPort,
                socksProxyUsername,
                socksProxyPassword,
                ioTimeoutMs
        );
    }

    private Http2SinkConfiguration.Http2ClientConfiguration.RequestConfiguration loadRequestConfiguration(final Map<String, String> properties) {

        final int maxRetries = parseIntProperty(properties, STATIC_HTTP2_SINK_REQUEST_MAX_RETRIES, DEFAULT_REQUEST_MAX_RETRIES);
        final long retriesIntervalMs = parseLongProperty(properties, STATIC_HTTP2_SINK_REQUEST_RETRY_INTERVAL_MS, DEFAULT_REQUEST_RETRY_INTERVAL_MS);
        final boolean authenticationEnabled = parseBooleanProperty(properties, STATIC_HTTP2_SINK_REQUEST_AUTH_ENABLED);
        final boolean circularRedirectsAllowed = parseBooleanProperty(properties, STATIC_HTTP2_SINK_REQUEST_CIRCULAR_REDIRECTS_ALLOWED);
        final long keepAliveMs = parseLongProperty(properties, STATIC_HTTP2_SINK_REQUEST_KEEP_ALIVE_MS, DEFAULT_REQUEST_KEEP_ALIVE_INTERVAL_MS);
        final long connectionManagerRequestTimeoutMs = parseLongProperty(properties, STATIC_HTTP2_SINK_REQUEST_CONN_MANAGER_TIMEOUT_MS, DEFAULT_REQUEST_CONN_MANAGER_TIMEOUT_MS);
        final boolean expectContinueEnabled = parseBooleanProperty(properties, STATIC_HTTP2_SINK_REQUEST_EXPECT_CONTINUE);
        final int maxRedirects = parseIntProperty(properties,STATIC_HTTP2_SINK_REQUEST_MAX_REDIRECTS, DEFAULT_REQUEST_MAX_REDIRECTS);
        final boolean disableProtocolUpgrade = parseBooleanProperty(properties, STATIC_HTTP2_SINK_REQUEST_DISABLE_PROTOCOL_UPGRADE);
        final int maxFrameSize = parseIntProperty(properties, STATIC_HTTP2_SINK_REQUEST_MAX_FRAME_SIZE, DEFAULT_REQUEST_MAX_FRAME_SIZE);
        final boolean compressionDisabled = parseBooleanProperty(properties, STATIC_HTTP2_SINK_REQUEST_DISABLE_COMPRESSION);
        final int maxConcurrentStreams = parseIntProperty(properties, STATIC_HTTP2_SINK_REQUEST_MAX_CONCURRENT_STREAMS, Integer.MAX_VALUE);
        final int initialWindowSize = parseIntProperty(properties, STATIC_HTTP2_SINK_REQUEST_INITIAL_WINDOW_SIZE, DEFAULT_REQUEST_INITIAL_WINDOW_SIZE);
        
        return new Http2SinkConfiguration.Http2ClientConfiguration.RequestConfiguration(
                maxRetries,
                retriesIntervalMs,
                authenticationEnabled,
                circularRedirectsAllowed,
                keepAliveMs,
                connectionManagerRequestTimeoutMs,
                expectContinueEnabled,
                maxRedirects,
                disableProtocolUpgrade,
                maxFrameSize,
                compressionDisabled,
                maxConcurrentStreams,
                initialWindowSize
        );
    }

    private Http2SinkConfiguration.Http2ClientConfiguration.TLSConfiguration loadTlsConfiguration(final Map<String, String> properties) {

        final boolean useTls = parseBooleanProperty(properties, STATIC_HTTP2_SINK_TLS_ENABLED);
        if (!useTls) {
            return null;
        }

        final boolean useMutualTls = parseBooleanProperty(properties, STATIC_HTTP2_SINK_MTLS_ENABLED);
        final String trustStoreType = findRequiredProperty(properties, STATIC_HTTP2_SINK_TLS_TRUSTSTORE_TYPE);
        final String trustStoreLocation = findRequiredProperty(properties, STATIC_HTTP2_SINK_TLS_TRUSTSTORE_LOCATION);
        final String trustStorePwd = properties.getOrDefault(STATIC_HTTP2_SINK_TLS_TRUSTSTORE_PWD, "");
        final Http2SinkConfiguration.Http2ClientConfiguration.TLSConfiguration.KStore trustStore =
                new Http2SinkConfiguration.Http2ClientConfiguration.TLSConfiguration.KStore(
                        trustStoreType,
                        trustStoreLocation,
                        trustStorePwd.toCharArray()
                );
        final String tlsProtocol = properties.getOrDefault(STATIC_HTTP2_SINK_TLS_PROTOCOL, DEFAULT_TLS_PROTOCOL);

        if (!useMutualTls) {
            return new Http2SinkConfiguration.Http2ClientConfiguration.TLSConfiguration(tlsProtocol, trustStore);
        }

        final String keyStoreType = findRequiredProperty(properties, STATIC_HTTP2_SINK_TLS_KEYSTORE_TYPE);
        final String keyStoreLocation = findRequiredProperty(properties, STATIC_HTTP2_SINK_TLS_KEYSTORE_LOCATION);
        final String keyStorePwd = properties.getOrDefault(STATIC_HTTP2_SINK_TLS_KEYSTORE_PWD, "");
        final Http2SinkConfiguration.Http2ClientConfiguration.TLSConfiguration.KStore keyStore =
                new Http2SinkConfiguration.Http2ClientConfiguration.TLSConfiguration.KStore(
                        keyStoreType,
                        keyStoreLocation,
                        keyStorePwd.toCharArray()
                );
        final String keyPwd = properties.getOrDefault(STATIC_HTTP2_SINK_TLS_KEYSTORE_KEY_PWD, "");

        return new Http2SinkConfiguration.Http2ClientConfiguration.MutualTLSConfiguration(
                tlsProtocol,
                trustStore,
                keyStore,
                keyPwd.toCharArray()
        );
    }

    private Http2SinkConfiguration.Http2ClientConfiguration.AuthenticationConfiguration loadAuthConfiguration(final Map<String, String> properties) {

        final String authProviderStr = properties.get(STATIC_HTTP2_SINK_AUTH_PROVIDER);
        final Http2SinkConfiguration.Http2ClientConfiguration.AuthenticationConfiguration.AuthProvider authProvider = Http2SinkConfiguration.Http2ClientConfiguration.AuthenticationConfiguration.AuthProvider.parse(authProviderStr);

        final String realm = properties.get(STATIC_HTTP2_SINK_AUTH_REALM);
        final String host = findRequiredProperty(properties, STATIC_HTTP2_SINK_MESSAGE_OUTPUT_HOST);
        final int port = parseIntProperty(properties, STATIC_HTTP2_SINK_MESSAGE_OUTPUT_PORT, DEFAULT_PORT);
        final String scheme = properties.getOrDefault(STATIC_HTTP2_SINK_MESSAGE_OUTPUT_SCHEME, DEFAULT_SCHEME);

        final Http2SinkConfiguration.Http2ClientConfiguration.AuthenticationConfiguration.Scope scope = new Http2SinkConfiguration.Http2ClientConfiguration.AuthenticationConfiguration.Scope(realm, scheme, host, port);
        final Http2SinkConfiguration.Http2ClientConfiguration.AuthenticationConfiguration.Credentials credentials = loadCredentialsContainer(authProvider, properties);

        return new Http2SinkConfiguration.Http2ClientConfiguration.AuthenticationConfiguration(
                authProvider,
                scope,
                credentials
        );
    }

    private Http2SinkConfiguration.Http2ClientConfiguration.AuthenticationConfiguration.Credentials loadCredentialsContainer(
            final Http2SinkConfiguration.Http2ClientConfiguration.AuthenticationConfiguration.AuthProvider authProvider,
            final Map<String, String> properties) {
        if (authProvider != Http2SinkConfiguration.Http2ClientConfiguration.AuthenticationConfiguration.AuthProvider.BASIC) {
            return null;
        }

        final String username = findRequiredProperty(properties, STATIC_HTTP2_SINK_AUTH_USERNAME);
        final String password = findRequiredProperty(properties, STATIC_HTTP2_SINK_AUTH_PWD);

        return new Http2SinkConfiguration.Http2ClientConfiguration.AuthenticationConfiguration.BasicCredentials(username, password.toCharArray());
    }

    private Http2SinkConfiguration.Http2ClientConfiguration.ConnectionConfiguration loadConnectionConfiguration(final Map<String, String> properties) {

        final long idleConnectionTimeoutMs = parseLongProperty(properties, STATIC_HTTP2_SINK_IDLE_TIMEOUT_MS, DEFAULT_CONN_IDLE_TIMEOUT_MS);
        final long validateAfterInactivityIntervalMs = parseLongProperty(properties, STATIC_HTTP2_SINK_VALIDATE_AFTER_INACTIVITY_MS, DEFAULT_CONN_VALIDATE_AFTER_INACTIVITY_MS);
        final long socketTimeoutMs = parseLongProperty(properties, STATIC_HTTP2_SINK_CONN_SOCKET_TIMEOUT_MS, DEFAULT_CONN_SOCKET_TIMEOUT_MS);
        final long connectTimeoutMs = parseLongProperty(properties, STATIC_HTTP2_SINK_CONN_CONNECT_TIMEOUT_MS, DEFAULT_CONN_CONNECT_TIMEOUT_MS);
        final long connectionTimeToLiveMs = parseLongProperty(properties, STATIC_HTTP2_SINK_CONN_TTL_MS, DEFAULT_CONN_TTL_MS);

        return new Http2SinkConfiguration.Http2ClientConfiguration.ConnectionConfiguration(
                idleConnectionTimeoutMs,
                validateAfterInactivityIntervalMs,
                socketTimeoutMs,
                connectTimeoutMs,
                connectionTimeToLiveMs
        );
    }

    private Map<String, String> createMapping(final String mappingString) {
        if (mappingString == null || mappingString.isEmpty()) {
            return Collections.emptyMap();
        }

        return Arrays.stream(mappingString.split(";"))
                        .map(mappingParts -> mappingParts.split(":"))
                        .collect(Collectors.toMap(mapping -> mapping[0], mapping -> mapping[mapping.length - 1]));
    }

    private static int power(final int base, final int degree) {
        return degree == 0 ? base : base * power(base, degree - 1);
    }
}
