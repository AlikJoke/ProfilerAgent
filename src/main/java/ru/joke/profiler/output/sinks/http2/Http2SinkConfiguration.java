package ru.joke.profiler.output.sinks.http2;

import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.meta.ProfilerDefaultEnumProperty;
import ru.joke.profiler.configuration.util.MillisTimePropertyParser;
import ru.joke.profiler.output.sinks.async.AsyncOutputDataSinkConfigurationSupport;
import ru.joke.profiler.output.sinks.async.AsyncSinkDataFlushingConfiguration;
import ru.joke.profiler.output.sinks.util.parsers.OutputDataPropertiesMappingConfigurationPropertyParser;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;

import static ru.joke.profiler.output.sinks.http2.OutputDataHttp2SinkHandle.SINK_TYPE;
import static ru.joke.profiler.util.ArgUtil.*;

public final class Http2SinkConfiguration extends AsyncOutputDataSinkConfigurationSupport {

    private static final String HTTP2_SINK_PROPERTIES_PREFIX = SINK_TYPE + ".";
    
    private final OutputMessageConfiguration outputMessageConfiguration;
    private final ProcessingConfiguration processingConfiguration;
    private final Http2ClientConfiguration http2ClientConfiguration;
    private final OutputEndpointConfiguration outputEndpointConfiguration;

    @ProfilerConfigurationPropertiesWrapper(prefix = HTTP2_SINK_PROPERTIES_PREFIX)
    public Http2SinkConfiguration(
            final OutputMessageConfiguration outputMessageConfiguration,
            final ProcessingConfiguration processingConfiguration,
            final Http2ClientConfiguration http2ClientConfiguration,
            final OutputEndpointConfiguration outputEndpointConfiguration,
            final AsyncSinkDataFlushingConfiguration asyncFlushingConfiguration
    ) {
        super(asyncFlushingConfiguration);
        this.outputMessageConfiguration = checkNotNull(outputMessageConfiguration, "configuration");
        this.outputEndpointConfiguration = checkNotNull(outputEndpointConfiguration, "outputEndpointConfiguration");
        this.processingConfiguration = checkNotNull(processingConfiguration, "processingConfiguration");
        this.http2ClientConfiguration = checkNotNull(http2ClientConfiguration, "http2ClientConfiguration");
    }

    public OutputMessageConfiguration outputMessageConfiguration() {
        return outputMessageConfiguration;
    }

    public ProcessingConfiguration processingConfiguration() {
        return processingConfiguration;
    }

    public Http2ClientConfiguration http2ClientConfiguration() {
        return http2ClientConfiguration;
    }

    public OutputEndpointConfiguration outputEndpointConfiguration() {
        return outputEndpointConfiguration;
    }

    @Override
    public String toString() {
        return "Http2SinkConfiguration{"
                + "outputMessageConfiguration=" + outputMessageConfiguration
                + ", processingConfiguration=" + processingConfiguration
                + ", http2ClientConfiguration=" + http2ClientConfiguration
                + ", outputEndpointConfiguration=" + outputEndpointConfiguration
                + ", asyncFlushingConfiguration=" + asyncFlushingConfiguration
                + '}';
    }

    public static class OutputEndpointConfiguration {

        private static final String OUTPUT_ENDPOINT_PROPERTIES_PREFIX = "output-endpoint.";

        private static final String OUTPUT_ENDPOINT = "target_endpoint";
        private static final String OUTPUT_HOST = "target_host";
        private static final String OUTPUT_PORT = "target_port";
        private static final String OUTPUT_SCHEME = "target_scheme";

        private final String outputScheme;
        private final String outputHost;
        private final int outputPort;
        private final String outputEndpoint;
        private final AuthenticationConfiguration authenticationConfiguration;

        @ProfilerConfigurationPropertiesWrapper(prefix = OUTPUT_ENDPOINT_PROPERTIES_PREFIX)
        public OutputEndpointConfiguration(
                @ProfilerConfigurationProperty(name = OUTPUT_SCHEME, defaultValue = "http") final String outputScheme,
                @ProfilerConfigurationProperty(name = OUTPUT_HOST, required = true) final String outputHost,
                @ProfilerConfigurationProperty(name = OUTPUT_PORT, defaultValue = "-1") final int outputPort,
                @ProfilerConfigurationProperty(name = OUTPUT_ENDPOINT, required = true) final String outputEndpoint,
                final AuthenticationConfiguration authenticationConfiguration
        ) {
            this.outputScheme = checkNotEmpty(outputScheme, "outputScheme");
            this.outputHost = checkNotEmpty(outputHost, "outputHost");
            this.outputPort = outputPort;
            this.outputEndpoint = checkNotEmpty(outputEndpoint, "outputEndpoint");
            this.authenticationConfiguration = checkNotNull(authenticationConfiguration, "authenticationConfiguration");
        }

        public String outputScheme() {
            return outputScheme;
        }

        public String outputHost() {
            return outputHost;
        }

        public int outputPort() {
            return outputPort;
        }

        public String outputEndpoint() {
            return outputEndpoint;
        }

        public AuthenticationConfiguration authenticationConfiguration() {
            return authenticationConfiguration;
        }

        @Override
        public String toString() {
            return "OutputEndpointConfiguration{"
                    + "outputScheme='" + outputScheme + '\''
                    + ", outputHost='" + outputHost + '\''
                    + ", outputPort=" + outputPort
                    + ", outputEndpoint='" + outputEndpoint + '\''
                    + ", authenticationConfiguration=" + authenticationConfiguration
                    + '}';
        }

        public static class AuthenticationConfiguration {

            private static final String AUTH_CONFIGURATION_PREFIX = "auth.";

            private static final String PROVIDER = "provider";
            private static final String REALM = "realm";

            private final AuthProvider provider;
            private final String realm;
            private final Credentials credentials;

            @ProfilerConfigurationPropertiesWrapper(prefix = AUTH_CONFIGURATION_PREFIX)
            public AuthenticationConfiguration(
                    @ProfilerConfigurationProperty(name = PROVIDER) final AuthProvider provider,
                    @ProfilerConfigurationProperty(name = REALM) final String realm,
                    @ProfilerConfigurationPropertiesWrapper(conditionalOn = PROVIDER, parser = Http2CredentialsConfigurationParser.class) final Credentials credentials
            ) {
                this.provider = provider;
                this.realm = realm;
                this.credentials = credentials;
            }

            public AuthProvider provider() {
                return provider;
            }

            public String realm() {
                return realm;
            }

            public Credentials credentials() {
                return credentials;
            }

            @Override
            public String toString() {
                return "AuthenticationConfiguration{"
                        + "provider=" + provider
                        + ", realm=" + realm
                        + ", credentials=" + credentials
                        + '}';
            }

            public enum AuthProvider {

                SYSTEM_DEFAULT(null),

                BASIC(BasicCredentials.class),

                @ProfilerDefaultEnumProperty
                NONE(null);

                private final Class<? extends Credentials> credentialsType;

                AuthProvider(final Class<? extends Credentials> credentialsType) {
                    this.credentialsType = credentialsType;
                }

                Class<? extends Credentials> credentialsType() {
                    return credentialsType;
                }
            }

            public static class Credentials {

                @ProfilerConfigurationPropertiesWrapper
                public Credentials() {}
            }

            public static class BasicCredentials extends Credentials {

                protected static final String BASIC_CREDENTIALS_PREFIX = "basic-credentials.";

                private static final String USERNAME = "username";
                private static final String PWD = "password";

                private final String username;
                private final char[] password;

                @ProfilerConfigurationPropertiesWrapper(prefix = BASIC_CREDENTIALS_PREFIX)
                public BasicCredentials(
                        @ProfilerConfigurationProperty(name = USERNAME) final String username,
                        @ProfilerConfigurationProperty(name = PWD) final char[] password
                ) {
                    this.username = checkNotEmpty(username, "username");
                    this.password = checkNotNull(password, "password");
                }

                public String username() {
                    return username;
                }

                public char[] password() {
                    return password;
                }

                @Override
                public String toString() {
                    return "BasicCredentials{"
                            + "username='" + username + '\''
                            + '}';
                }
            }
        }
    }

    public static class OutputMessageConfiguration {

        private static final String OUTPUT_MESSAGE_PROPERTIES_PREFIX = "output-message.";

        private static final String CONTENT_TYPE = "content_type";
        private static final String BODY_PROPERTIES_MAPPING = "body_mapping";
        private static final String HEADERS_MAPPING = "headers_mapping";

        private final String contentType;
        private final Map<String, String> propertiesMapping;
        private final Map<String, String> headersMapping;

        @ProfilerConfigurationPropertiesWrapper(prefix = OUTPUT_MESSAGE_PROPERTIES_PREFIX)
        public OutputMessageConfiguration(
                @ProfilerConfigurationProperty(name = CONTENT_TYPE, defaultValue = "application/json") final String contentType,
                @ProfilerConfigurationProperty(name = BODY_PROPERTIES_MAPPING, parser = OutputDataPropertiesMappingConfigurationPropertyParser.class) final Map<String, String> propertiesMapping,
                @ProfilerConfigurationProperty(name = HEADERS_MAPPING, parser = OutputDataPropertiesMappingConfigurationPropertyParser.class) final Map<String, String> headersMapping
        ) {
            this.contentType = checkNotEmpty(contentType, "contentType");
            this.headersMapping = Collections.unmodifiableMap(headersMapping);
            this.propertiesMapping = Collections.unmodifiableMap(propertiesMapping);
        }

        public String contentType() {
            return contentType;
        }

        public Map<String, String> propertiesMapping() {
            return propertiesMapping;
        }

        public Map<String, String> headersMapping() {
            return headersMapping;
        }

        @Override
        public String toString() {
            return "OutputMessageConfiguration{"
                    + ", contentType='" + contentType + '\''
                    + ", propertiesMapping=" + propertiesMapping
                    + ", headersMapping=" + headersMapping
                    + '}';
        }
    }

    public static class ProcessingConfiguration {

        private static final String PROCESSING_CONFIGURATION_PREFIX = "processing.";

        private static final String DISABLE_ASYNC_SENDING = "disable_async_sending";
        private static final String ON_SENDING_ERROR_POLICY = "on_error_policy";
        private static final String MAX_RETRIES_ON_ERROR = "max_retries";
        private static final String SYNC_SENDING_WAIT = "sync_wait";

        private final boolean disableAsyncSending;
        private final OnErrorPolicy onErrorPolicy;
        private final int maxRetriesOnError;
        private final long syncSendingWaitMs;

        @ProfilerConfigurationPropertiesWrapper(prefix = PROCESSING_CONFIGURATION_PREFIX)
        public ProcessingConfiguration(
                @ProfilerConfigurationProperty(name = DISABLE_ASYNC_SENDING) final boolean disableAsyncSending,
                @ProfilerConfigurationProperty(name = ON_SENDING_ERROR_POLICY) final OnErrorPolicy onErrorPolicy,
                @ProfilerConfigurationProperty(name = MAX_RETRIES_ON_ERROR, defaultValue = "2") final int maxRetriesOnError,
                @ProfilerConfigurationProperty(name = SYNC_SENDING_WAIT, defaultValue = "5s", parser = MillisTimePropertyParser.class) final long syncSendingWaitMs
        ) {
            this.disableAsyncSending = disableAsyncSending;
            this.onErrorPolicy = checkNotNull(onErrorPolicy, "onErrorPolicy");
            this.maxRetriesOnError = checkNonNegative(maxRetriesOnError, "maxRetriesOnError");
            this.syncSendingWaitMs = checkNonNegative(syncSendingWaitMs, "syncSendingWaitMs");
        }

        public long syncSendingWaitMs() {
            return syncSendingWaitMs;
        }

        public boolean disableAsyncSending() {
            return disableAsyncSending;
        }

        public OnErrorPolicy onErrorPolicy() {
            return onErrorPolicy;
        }

        public int maxRetriesOnError() {
            return maxRetriesOnError;
        }

        @Override
        public String toString() {
            return "ProcessingConfiguration{"
                    + "disableAsyncSending=" + disableAsyncSending
                    + ", onErrorPolicy=" + onErrorPolicy
                    + ", maxRetriesOnError=" + maxRetriesOnError
                    + ", syncSendingWaitMs=" + syncSendingWaitMs
                    + '}';
        }

        public enum OnErrorPolicy {

            SKIP,

            @ProfilerDefaultEnumProperty
            RETRY
        }
    }

    public static class Http2ClientConfiguration {

        private static final String CLIENT_CONFIGURATION_PREFIX = "client.";

        private static final String TLS_CONFIGURATION_PREFIX = "tls.";
        private static final String GRACEFUL_SHUTDOWN_ENABLED = "graceful_shutdown";

        private final ConnectionConfiguration connectionConfiguration;
        private final TLSConfiguration tlsConfiguration;
        private final RequestConfiguration requestConfiguration;
        private final IOConfiguration ioConfiguration;
        private final boolean enableGracefulShutdown;

        @ProfilerConfigurationPropertiesWrapper(prefix = CLIENT_CONFIGURATION_PREFIX)
        public Http2ClientConfiguration(
                final ConnectionConfiguration connectionConfiguration,
                @ProfilerConfigurationPropertiesWrapper(prefix = TLS_CONFIGURATION_PREFIX, parser = TLSConfigurationParser.class) final TLSConfiguration tlsConfiguration,
                final RequestConfiguration requestConfiguration,
                final IOConfiguration ioConfiguration,
                @ProfilerConfigurationProperty(name = GRACEFUL_SHUTDOWN_ENABLED) final boolean enableGracefulShutdown
        ) {
            this.connectionConfiguration = checkNotNull(connectionConfiguration, "connectionConfiguration");
            this.tlsConfiguration = tlsConfiguration;
            this.requestConfiguration = checkNotNull(requestConfiguration, "requestConfiguration");
            this.ioConfiguration = checkNotNull(ioConfiguration, "ioConfiguration");
            this.enableGracefulShutdown = enableGracefulShutdown;
        }

        public boolean enableGracefulShutdown() {
            return enableGracefulShutdown;
        }

        public TLSConfiguration tlsConfiguration() {
            return tlsConfiguration;
        }

        public ConnectionConfiguration connectionConfiguration() {
            return connectionConfiguration;
        }

        public RequestConfiguration requestConfiguration() {
            return requestConfiguration;
        }

        public IOConfiguration ioConfiguration() {
            return ioConfiguration;
        }

        @Override
        public String toString() {
            return "Http2ClientConfiguration{"
                    + ", connectionConfiguration=" + connectionConfiguration
                    + ", tlsConfiguration=" + tlsConfiguration
                    + ", requestConfiguration=" + requestConfiguration
                    + ", ioConfiguration=" + ioConfiguration
                    + ", enableGracefulShutdown=" + enableGracefulShutdown
                    + '}';
        }

        public static class RequestConfiguration {

            private static final String REQUEST_CONFIGURATION_PREFIX = "request.";

            private static final String MAX_RETRIES = "max_retries";
            private static final String RETRY_INTERVAL = "retry_interval";
            private static final String AUTH_ENABLED = "auth_enabled";
            private static final String CIRCULAR_REDIRECTS_ALLOWED = "circular_redirects_allowed";
            private static final String KEEP_ALIVE = "keep_alive";
            private static final String CONN_MANAGER_TIMEOUT = "conn_manager_timeout";
            private static final String DISABLE_COMPRESSION = "disable_compression";
            private static final String EXPECT_CONTINUE = "expect_continue";
            private static final String MAX_REDIRECTS = "max_redirects";
            private static final String DISABLE_PROTOCOL_UPGRADE = "disable_protocol_upgrade";
            private static final String MAX_FRAME_SIZE = "max_frame_size";
            private static final String MAX_CONCURRENT_STREAMS = "max_concurrent_streams";
            private static final String INITIAL_WINDOW_SIZE = "initial_window_size";

            private final int maxRetries;
            private final long retriesIntervalMs;
            private final boolean authenticationEnabled;
            private final boolean circularRedirectsAllowed;
            private final long keepAliveMs;
            private final long connectionManagerRequestTimeoutMs;
            private final boolean expectContinueEnabled;
            private final int maxRedirects;
            private final boolean disableProtocolUpgrade;
            private final int maxFrameSize;
            private final boolean compressionDisabled;
            private final int maxConcurrentStreams;
            private final int initialWindowSize;

            @ProfilerConfigurationPropertiesWrapper(prefix = REQUEST_CONFIGURATION_PREFIX)
            public RequestConfiguration(
                    @ProfilerConfigurationProperty(name = MAX_RETRIES, defaultValue = "3") final int maxRetries,
                    @ProfilerConfigurationProperty(name = RETRY_INTERVAL, defaultValue = "5s", parser = MillisTimePropertyParser.class) final long retriesIntervalMs,
                    @ProfilerConfigurationProperty(name = AUTH_ENABLED) final boolean authenticationEnabled,
                    @ProfilerConfigurationProperty(name = CIRCULAR_REDIRECTS_ALLOWED) final boolean circularRedirectsAllowed,
                    @ProfilerConfigurationProperty(name = KEEP_ALIVE, defaultValue = "3m", parser = MillisTimePropertyParser.class) final long keepAliveMs,
                    @ProfilerConfigurationProperty(name = CONN_MANAGER_TIMEOUT, defaultValue = "2m", parser = MillisTimePropertyParser.class) final long connectionManagerRequestTimeoutMs,
                    @ProfilerConfigurationProperty(name = EXPECT_CONTINUE) final boolean expectContinueEnabled,
                    @ProfilerConfigurationProperty(name = MAX_REDIRECTS, defaultValue = "0") final int maxRedirects,
                    @ProfilerConfigurationProperty(name = DISABLE_PROTOCOL_UPGRADE) final boolean disableProtocolUpgrade,
                    @ProfilerConfigurationProperty(name = MAX_FRAME_SIZE, defaultValue = "1048576") final int maxFrameSize,
                    @ProfilerConfigurationProperty(name = DISABLE_COMPRESSION) final boolean compressionDisabled,
                    @ProfilerConfigurationProperty(name = MAX_CONCURRENT_STREAMS, defaultValue = "-1") final int maxConcurrentStreams,
                    @ProfilerConfigurationProperty(name = INITIAL_WINDOW_SIZE, defaultValue = "65535") final int initialWindowSize
            ) {
                this.maxRetries = checkNonNegative(maxRetries, "maxRetries");
                this.retriesIntervalMs = checkNonNegative(retriesIntervalMs, "retriesIntervalMs");
                this.authenticationEnabled = authenticationEnabled;
                this.circularRedirectsAllowed = circularRedirectsAllowed;
                this.keepAliveMs = checkNonNegative(keepAliveMs, "keepAliveMs");
                this.connectionManagerRequestTimeoutMs = checkNonNegative(connectionManagerRequestTimeoutMs, "connectionManagerRequestTimeoutMs");
                this.expectContinueEnabled = expectContinueEnabled;
                this.maxRedirects = checkNonNegative(maxRedirects, "maxRedirects");
                this.disableProtocolUpgrade = disableProtocolUpgrade;
                this.maxFrameSize = checkPositive(maxFrameSize, "maxFrameSize");
                this.compressionDisabled = compressionDisabled;
                this.maxConcurrentStreams = maxConcurrentStreams == -1 ? Integer.MAX_VALUE : checkPositive(maxConcurrentStreams, "maxConcurrentStreams");
                this.initialWindowSize = checkPositive(initialWindowSize, "initialWindowSize");
            }

            public int maxRetries() {
                return maxRetries;
            }

            public long retriesIntervalMs() {
                return retriesIntervalMs;
            }

            public boolean authenticationEnabled() {
                return authenticationEnabled;
            }

            public boolean circularRedirectsAllowed() {
                return circularRedirectsAllowed;
            }

            public long keepAliveMs() {
                return keepAliveMs;
            }

            public long connectionManagerRequestTimeoutMs() {
                return connectionManagerRequestTimeoutMs;
            }

            public boolean expectContinueEnabled() {
                return expectContinueEnabled;
            }

            public int maxRedirects() {
                return maxRedirects;
            }

            public boolean disableProtocolUpgrade() {
                return disableProtocolUpgrade;
            }

            public int maxFrameSize() {
                return maxFrameSize;
            }

            public boolean compressionEnabled() {
                return !compressionDisabled;
            }

            public int maxConcurrentStreams() {
                return maxConcurrentStreams;
            }

            public int initialWindowSize() {
                return initialWindowSize;
            }

            @Override
            public String toString() {
                return "RequestConfiguration{"
                        + "maxRetries=" + maxRetries
                        + ", retriesIntervalMs=" + retriesIntervalMs
                        + ", authenticationEnabled=" + authenticationEnabled
                        + ", circularRedirectsAllowed=" + circularRedirectsAllowed
                        + ", keepAliveMs=" + keepAliveMs
                        + ", connectionManagerRequestTimeoutMs=" + connectionManagerRequestTimeoutMs
                        + ", expectContinueEnabled=" + expectContinueEnabled
                        + ", maxRedirects=" + maxRedirects
                        + ", disableProtocolUpgrade=" + disableProtocolUpgrade
                        + ", maxFrameSize=" + maxFrameSize
                        + ", compressionDisabled=" + compressionDisabled
                        + ", maxConcurrentStreams=" + maxConcurrentStreams
                        + ", initialWindowSize=" + initialWindowSize
                        + '}';
            }
        }

        public static class ConnectionConfiguration {
            
            private static final String CONNECTION_CONFIGURATION_PREFIX = "connection.";

            private static final String IDLE_TIMEOUT = "idle_timeout";
            private static final String VALIDATE_AFTER_INACTIVITY = "validate_after_inactivity_interval";
            private static final String SOCKET_TIMEOUT = "socket_timeout";
            private static final String CONNECT_TIMEOUT = "connect_timeout";
            private static final String TTL = "time_to_live";

            private final long idleConnectionTimeoutMs;
            private final long validateAfterInactivityIntervalMs;
            private final long socketTimeoutMs;
            private final long connectTimeoutMs;
            private final long connectionTimeToLiveMs;

            @ProfilerConfigurationPropertiesWrapper(prefix = CONNECTION_CONFIGURATION_PREFIX)
            public ConnectionConfiguration(
                    @ProfilerConfigurationProperty(name = IDLE_TIMEOUT, defaultValue = "2m", parser = MillisTimePropertyParser.class) final long idleConnectionTimeoutMs,
                    @ProfilerConfigurationProperty(name = VALIDATE_AFTER_INACTIVITY, defaultValue = "1m", parser = MillisTimePropertyParser.class) final long validateAfterInactivityIntervalMs,
                    @ProfilerConfigurationProperty(name = SOCKET_TIMEOUT, defaultValue = "30s", parser = MillisTimePropertyParser.class) final long socketTimeoutMs,
                    @ProfilerConfigurationProperty(name = CONNECT_TIMEOUT, defaultValue = "30s", parser = MillisTimePropertyParser.class) final long connectTimeoutMs,
                    @ProfilerConfigurationProperty(name = TTL, defaultValue = "-1", parser = MillisTimePropertyParser.class) final long connectionTimeToLiveMs
            ) {
                this.idleConnectionTimeoutMs = idleConnectionTimeoutMs;
                this.validateAfterInactivityIntervalMs = validateAfterInactivityIntervalMs;
                this.socketTimeoutMs = socketTimeoutMs;
                this.connectTimeoutMs = connectTimeoutMs;
                this.connectionTimeToLiveMs = connectionTimeToLiveMs;
            }

            public long idleConnectionTimeoutMs() {
                return idleConnectionTimeoutMs;
            }

            public long validateAfterInactivityIntervalMs() {
                return validateAfterInactivityIntervalMs;
            }

            public long socketTimeoutMs() {
                return socketTimeoutMs;
            }

            public long connectTimeoutMs() {
                return connectTimeoutMs;
            }

            public long connectionTimeToLiveMs() {
                return connectionTimeToLiveMs;
            }

            @Override
            public String toString() {
                return "ConnectionConfiguration{"
                        + "idleConnectionTimeoutMs=" + idleConnectionTimeoutMs
                        + ", validateAfterInactivityIntervalMs=" + validateAfterInactivityIntervalMs
                        + ", socketTimeoutMs=" + socketTimeoutMs
                        + ", connectTimeoutMs=" + connectTimeoutMs
                        + ", connectionTimeToLiveMs=" + connectionTimeToLiveMs
                        + '}';
            }
        }

        public static class IOConfiguration {

            private static final String IO_CONFIGURATION_PREFIX = "io.";

            private static final String THREAD_COUNT = "thread_count";
            private static final String SEND_BUFFER_SIZE = "send_buffer_size";
            private static final String LINGER = "linger";
            private static final String TCP_NO_DELAY = "tcp_no_delay";
            private static final String TCP_KEEP_ALIVE_PROBE_INTERVAL = "tcp_keep_alive_probe_interval";
            private static final String TCP_IDLE_TIMEOUT = "tcp_idle_timeout";
            private static final String MAX_KEEP_ALIVE_PROBES_BEFORE_DROP = "tcp_max_keep_alive_probes_before_drop";
            private static final String SOCKS_PROXY_HOST = "socks_proxy_host";
            private static final String SOCKS_PROXY_PORT = "socks_proxy_port";
            private static final String SOCKS_PROXY_USERNAME = "socks_proxy_username";
            private static final String SOCKS_PROXY_PWD = "socks_proxy_password";
            private static final String SOCKET_TIMEOUT = "socket_timeout";
            
            private final int threadCount;
            private final int sendBufferSize;
            private final long lingerTimeoutMs;
            private final boolean tcpNoDelay;
            private final long keepAliveIntervalMs;
            private final long idleTimeoutMs;
            private final int maxKeepAliveProbesBeforeDrop;
            private final String socksProxyHost;
            private final int socksProxyPort;
            private final String socksProxyUsername;
            private final char[] socksProxyPassword;
            private final long ioTimeoutMs;

            @ProfilerConfigurationPropertiesWrapper(prefix = IO_CONFIGURATION_PREFIX)
            public IOConfiguration(
                    @ProfilerConfigurationProperty(name = THREAD_COUNT, defaultValue = "2") final int threadCount,
                    @ProfilerConfigurationProperty(name = SEND_BUFFER_SIZE, defaultValue = "0") final int sendBufferSize,
                    @ProfilerConfigurationProperty(name = LINGER, defaultValue = "-1", parser = MillisTimePropertyParser.class) final long lingerTimeoutMs,
                    @ProfilerConfigurationProperty(name = TCP_NO_DELAY) final boolean tcpNoDelay,
                    @ProfilerConfigurationProperty(name = TCP_KEEP_ALIVE_PROBE_INTERVAL, defaultValue = "-1", parser = MillisTimePropertyParser.class) final long keepAliveIntervalMs,
                    @ProfilerConfigurationProperty(name = TCP_IDLE_TIMEOUT, defaultValue = "-1", parser = MillisTimePropertyParser.class)  final long idleTimeoutMs,
                    @ProfilerConfigurationProperty(name = MAX_KEEP_ALIVE_PROBES_BEFORE_DROP, defaultValue = "10") final int maxKeepAliveProbesBeforeDrop,
                    @ProfilerConfigurationProperty(name = SOCKS_PROXY_HOST) final String socksProxyHost,
                    @ProfilerConfigurationProperty(name = SOCKS_PROXY_PORT, defaultValue = "-1") final int socksProxyPort,
                    @ProfilerConfigurationProperty(name = SOCKS_PROXY_USERNAME) final String socksProxyUsername,
                    @ProfilerConfigurationProperty(name = SOCKS_PROXY_PWD) final char[] socksProxyPassword,
                    @ProfilerConfigurationProperty(name = SOCKET_TIMEOUT, defaultValue = "2s", parser = MillisTimePropertyParser.class) final long ioTimeoutMs
            ) {
                this.threadCount = checkPositive(threadCount, "threadCount");
                this.sendBufferSize = checkNonNegative(sendBufferSize, "sendBufferSize");
                this.lingerTimeoutMs = lingerTimeoutMs;
                this.tcpNoDelay = tcpNoDelay;
                this.keepAliveIntervalMs = keepAliveIntervalMs;
                this.idleTimeoutMs = idleTimeoutMs;
                this.maxKeepAliveProbesBeforeDrop = checkNonNegative(maxKeepAliveProbesBeforeDrop, "maxKeepAliveProbesBeforeDrop");
                this.socksProxyHost = socksProxyHost;
                this.socksProxyPort = socksProxyPort;
                this.socksProxyUsername = socksProxyUsername;
                this.socksProxyPassword = socksProxyPassword;
                this.ioTimeoutMs = checkPositive(ioTimeoutMs, "ioTimeoutMs");
            }

            public int threadCount() {
                return threadCount;
            }

            public int sendBufferSize() {
                return sendBufferSize;
            }

            public long lingerTimeoutMs() {
                return lingerTimeoutMs;
            }

            public boolean tcpNoDelay() {
                return tcpNoDelay;
            }

            public long keepAliveIntervalMs() {
                return keepAliveIntervalMs;
            }

            public long idleTimeoutMs() {
                return idleTimeoutMs;
            }

            public int maxKeepAliveProbesBeforeDrop() {
                return maxKeepAliveProbesBeforeDrop;
            }

            public String socksProxyHost() {
                return socksProxyHost;
            }

            public int socksProxyPort() {
                return socksProxyPort;
            }

            public String socksProxyUsername() {
                return socksProxyUsername;
            }

            public char[] socksProxyPassword() {
                return socksProxyPassword;
            }

            public long ioTimeoutMs() {
                return ioTimeoutMs;
            }

            @Override
            public String toString() {
                return "IOConfiguration{"
                        + "threadCount=" + threadCount
                        + ", sendBufferSize=" + sendBufferSize
                        + ", lingerTimeoutMs=" + lingerTimeoutMs
                        + ", tcpNoDelay=" + tcpNoDelay
                        + ", keepAliveIntervalMs=" + keepAliveIntervalMs
                        + ", idleTimeoutMs=" + idleTimeoutMs
                        + ", maxKeepAliveProbesBeforeDrop=" + maxKeepAliveProbesBeforeDrop
                        + ", socksProxyHost='" + socksProxyHost + '\''
                        + ", socksProxyPort=" + socksProxyPort
                        + ", socksProxyUsername='" + socksProxyUsername + '\''
                        + ", ioTimeoutMs=" + ioTimeoutMs + '}';
            }
        }

        public static class TLSConfiguration {
            
            protected static final String PROTOCOL = "protocol";
            protected static final String TRUSTSTORE_PREFIX = "truststore.";

            protected static final String DEFAULT_PROTOCOL = "TLSv1.2";

            protected final String protocol;
            protected final KStore trustStore;

            @ProfilerConfigurationPropertiesWrapper
            public TLSConfiguration(
                    @ProfilerConfigurationProperty(name = PROTOCOL, defaultValue = DEFAULT_PROTOCOL) final String protocol,
                    @ProfilerConfigurationPropertiesWrapper(prefix = TRUSTSTORE_PREFIX) final KStore trustStore) {
                this.protocol = checkNotEmpty(protocol, "protocol");
                this.trustStore = checkNotNull(trustStore, "trustStore");
            }

            public SSLContext createSSLContext() throws GeneralSecurityException, IOException {
                final KeyStore trustStore = KeyStore.getInstance(this.trustStore.type);
                try (final InputStream trustStoreInputStream = Files.newInputStream(Paths.get(this.trustStore.location))) {
                    trustStore.load(trustStoreInputStream, this.trustStore.password);
                }

                final SSLContextBuilder builder =
                        SSLContexts.custom()
                                .setProtocol(this.protocol)
                                .setSecureRandom(new SecureRandom())
                                .loadTrustMaterial(trustStore, null);
                enrichBuilder(builder);
                return builder.build();
            }

            void enrichBuilder(final SSLContextBuilder builder) throws GeneralSecurityException, IOException {

            }

            @Override
            public String toString() {
                return "TLSConfiguration{"
                        + "protocol='" + protocol + '\''
                        + ", trustStore=" + trustStore
                        + '}';
            }

            public static class KStore {

                private static final String TYPE = "type";
                private static final String LOCATION = "location";
                private static final String PWD = "password";

                private final String type;
                private final String location;
                private final char[] password;

                @ProfilerConfigurationPropertiesWrapper
                public KStore(
                        @ProfilerConfigurationProperty(name = TYPE, required = true) final String type,
                        @ProfilerConfigurationProperty(name = LOCATION, required = true) final String location,
                        @ProfilerConfigurationProperty(name = PWD, required = true) final char[] password
                ) {
                    this.type = checkNotEmpty(type, "type");
                    this.location = checkNotEmpty(location, "location");
                    this.password = checkNotNull(password, "password");
                }

                @Override
                public String toString() {
                    return "KStore{"
                            + "type='" + type + '\''
                            + ", location='" + location + '\''
                            + '}';
                }
            }
        }

        static class MutualTLSConfiguration extends TLSConfiguration {

            private static final String KEYSTORE_PREFIX = "keystore.";
            private static final String KEY_PWD = "key_password";

            private final KStore keyStore;
            private final char[] keyPassword;

            @ProfilerConfigurationPropertiesWrapper
            MutualTLSConfiguration(
                    @ProfilerConfigurationProperty(name = PROTOCOL, defaultValue = DEFAULT_PROTOCOL) final String protocol,
                    @ProfilerConfigurationPropertiesWrapper(prefix = TRUSTSTORE_PREFIX) final KStore trustStore,
                    @ProfilerConfigurationPropertiesWrapper(prefix = KEYSTORE_PREFIX) final KStore keyStore,
                    @ProfilerConfigurationProperty(name = KEY_PWD, required = true) final char[] keyPassword
            ) {
                super(protocol, trustStore);
                this.keyStore = checkNotNull(keyStore, "keyStore");
                this.keyPassword = checkNotNull(keyPassword, "keyPassword");
            }

            @Override
            void enrichBuilder(final SSLContextBuilder builder) throws GeneralSecurityException, IOException {
                builder
                        .setKeyStoreType(this.keyStore.type)
                        .loadKeyMaterial(new File(this.keyStore.location), this.keyStore.password, this.keyPassword);
            }

            @Override
            public String toString() {
                return "MutualTLSConfiguration{"
                        + "keyStore=" + keyStore
                        + ", protocol='" + protocol + '\''
                        + ", trustStore=" + trustStore
                        + '}';
            }
        }
    }
}
