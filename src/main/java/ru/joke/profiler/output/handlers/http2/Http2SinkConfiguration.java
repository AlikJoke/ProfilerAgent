package ru.joke.profiler.output.handlers.http2;

import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import ru.joke.profiler.configuration.InvalidConfigurationException;

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

final class Http2SinkConfiguration {

    private final OutputMessageConfiguration outputMessageConfiguration;
    private final ProcessingConfiguration processingConfiguration;
    private final Http2ClientConfiguration http2ClientConfiguration;

    Http2SinkConfiguration(
            final OutputMessageConfiguration outputMessageConfiguration,
            final ProcessingConfiguration processingConfiguration,
            final Http2ClientConfiguration http2ClientConfiguration) {
        this.outputMessageConfiguration = outputMessageConfiguration;
        this.processingConfiguration = processingConfiguration;
        this.http2ClientConfiguration = http2ClientConfiguration;
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

    @Override
    public String toString() {
        return "Http2SinkConfiguration{"
                + "outputMessageConfiguration=" + outputMessageConfiguration
                + ", processingConfiguration=" + processingConfiguration
                + ", http2ClientConfiguration=" + http2ClientConfiguration
                + '}';
    }

    static class OutputMessageConfiguration {

        private final String outputScheme;
        private final String outputHost;
        private final int outputPort;
        private final String outputEndpoint;
        private final String contentType;
        private final Map<String, String> propertiesMapping;
        private final Map<String, String> headersMapping;

        OutputMessageConfiguration(
                final String outputScheme,
                final String outputHost,
                final int outputPort,
                final String outputEndpoint,
                final String contentType,
                final Map<String, String> propertiesMapping,
                final Map<String, String> headersMapping) {
            this.outputScheme = outputScheme;
            this.outputHost = outputHost;
            this.outputPort = outputPort;
            this.outputEndpoint = outputEndpoint;
            this.contentType = contentType;
            this.headersMapping = Collections.unmodifiableMap(headersMapping);
            this.propertiesMapping = Collections.unmodifiableMap(propertiesMapping);
        }

        public String outputHost() {
            return outputHost;
        }

        public String outputScheme() {
            return outputScheme;
        }

        public int outputPort() {
            return outputPort;
        }

        public String outputEndpoint() {
            return outputEndpoint;
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
                    + "outputHost='" + outputHost + '\''
                    + ", outputPort=" + outputPort
                    + ", outputScheme=" + outputScheme + '\''
                    + ", outputEndpoint='" + outputEndpoint + '\''
                    + ", contentType='" + contentType + '\''
                    + ", propertiesMapping=" + propertiesMapping
                    + ", headersMapping=" + headersMapping
                    + '}';
        }
    }

    static class ProcessingConfiguration {

        private final boolean disableAsyncSending;
        private final OnErrorPolicy onErrorPolicy;
        private final int maxRetriesOnError;
        private final long syncSendingWaitMs;

        ProcessingConfiguration(
                final boolean disableAsyncSending,
                final OnErrorPolicy onErrorPolicy,
                final int maxRetriesOnError,
                final long syncSendingWaitMs) {
            this.disableAsyncSending = disableAsyncSending;
            this.onErrorPolicy = onErrorPolicy;
            this.maxRetriesOnError = maxRetriesOnError;
            this.syncSendingWaitMs = syncSendingWaitMs;
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

        enum OnErrorPolicy {

            SKIP,

            RETRY;

            static OnErrorPolicy parse(final String alias) {
                for (final OnErrorPolicy policy : values()) {
                    if (policy.name().equals(alias)) {
                        return policy;
                    }
                }

                if (alias == null || alias.isEmpty()) {
                    return SKIP;
                }

                throw new InvalidConfigurationException("Unknown type of policy: " + alias);
            }
        }
    }

    static class Http2ClientConfiguration {

        private final AuthenticationConfiguration authenticationConfiguration;
        private final ConnectionConfiguration connectionConfiguration;
        private final TLSConfiguration tlsConfiguration;
        private final RequestConfiguration requestConfiguration;
        private final IOConfiguration ioConfiguration;
        private final boolean enableGracefulShutdown;

        Http2ClientConfiguration(
                final AuthenticationConfiguration authenticationConfiguration,
                final ConnectionConfiguration connectionConfiguration,
                final TLSConfiguration tlsConfiguration,
                final RequestConfiguration requestConfiguration,
                final IOConfiguration ioConfiguration,
                final boolean enableGracefulShutdown) {
            this.authenticationConfiguration = authenticationConfiguration;
            this.connectionConfiguration = connectionConfiguration;
            this.tlsConfiguration = tlsConfiguration;
            this.requestConfiguration = requestConfiguration;
            this.ioConfiguration = ioConfiguration;
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

        public AuthenticationConfiguration authenticationConfiguration() {
            return authenticationConfiguration;
        }

        @Override
        public String toString() {
            return "Http2ClientConfiguration{"
                    + "authenticationConfiguration=" + authenticationConfiguration
                    + ", connectionConfiguration=" + connectionConfiguration
                    + ", tlsConfiguration=" + tlsConfiguration
                    + ", requestConfiguration=" + requestConfiguration
                    + ", ioConfiguration=" + ioConfiguration
                    + ", enableGracefulShutdown=" + enableGracefulShutdown
                    + '}';
        }

        static class RequestConfiguration {

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

            public RequestConfiguration(
                    final int maxRetries,
                    final long retriesIntervalMs,
                    final boolean authenticationEnabled,
                    final boolean circularRedirectsAllowed,
                    final long keepAliveMs,
                    final long connectionManagerRequestTimeoutMs,
                    final boolean expectContinueEnabled,
                    final int maxRedirects,
                    final boolean disableProtocolUpgrade,
                    final int maxFrameSize,
                    final boolean compressionDisabled,
                    final int maxConcurrentStreams,
                    final int initialWindowSize) {
                this.maxRetries = maxRetries;
                this.retriesIntervalMs = retriesIntervalMs;
                this.authenticationEnabled = authenticationEnabled;
                this.circularRedirectsAllowed = circularRedirectsAllowed;
                this.keepAliveMs = keepAliveMs;
                this.connectionManagerRequestTimeoutMs = connectionManagerRequestTimeoutMs;
                this.expectContinueEnabled = expectContinueEnabled;
                this.maxRedirects = maxRedirects;
                this.disableProtocolUpgrade = disableProtocolUpgrade;
                this.maxFrameSize = maxFrameSize;
                this.compressionDisabled = compressionDisabled;
                this.maxConcurrentStreams = maxConcurrentStreams;
                this.initialWindowSize = initialWindowSize;
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

        static class ConnectionConfiguration {

            private final long idleConnectionTimeoutMs;
            private final long validateAfterInactivityIntervalMs;
            private final long socketTimeoutMs;
            private final long connectTimeoutMs;
            private final long connectionTimeToLiveMs;

            ConnectionConfiguration(
                    final long idleConnectionTimeoutMs,
                    final long validateAfterInactivityIntervalMs,
                    final long socketTimeoutMs,
                    final long connectTimeoutMs,
                    final long connectionTimeToLiveMs) {
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

        static class IOConfiguration {

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

            IOConfiguration(
                    final int threadCount,
                    final int sendBufferSize,
                    final long lingerTimeoutMs,
                    final boolean tcpNoDelay,
                    final long keepAliveIntervalMs,
                    final long idleTimeoutMs,
                    final int maxKeepAliveProbesBeforeDrop,
                    final String socksProxyHost,
                    final int socksProxyPort,
                    final String socksProxyUsername,
                    final char[] socksProxyPassword,
                    final long ioTimeoutMs) {
                this.threadCount = threadCount;
                this.sendBufferSize = sendBufferSize;
                this.lingerTimeoutMs = lingerTimeoutMs;
                this.tcpNoDelay = tcpNoDelay;
                this.keepAliveIntervalMs = keepAliveIntervalMs;
                this.idleTimeoutMs = idleTimeoutMs;
                this.maxKeepAliveProbesBeforeDrop = maxKeepAliveProbesBeforeDrop;
                this.socksProxyHost = socksProxyHost;
                this.socksProxyPort = socksProxyPort;
                this.socksProxyUsername = socksProxyUsername;
                this.socksProxyPassword = socksProxyPassword;
                this.ioTimeoutMs = ioTimeoutMs;
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

        static class AuthenticationConfiguration {

            private final AuthProvider authProvider;
            private final Scope scope;
            private final Credentials credentials;

            AuthenticationConfiguration(
                    final AuthProvider authProvider,
                    final Scope scope,
                    final Credentials credentials) {
                this.authProvider = authProvider;
                this.scope = scope;
                this.credentials = credentials;
            }

            public AuthProvider authProvider() {
                return authProvider;
            }

            public Scope scope() {
                return scope;
            }

            public Credentials credentials() {
                return credentials;
            }

            @Override
            public String toString() {
                return "AuthenticationConfiguration{"
                        + "authProvider=" + authProvider
                        + ", scope=" + scope
                        + ", credentials=" + credentials
                        + '}';
            }

            enum AuthProvider {

                SYSTEM_DEFAULT,

                BASIC,

                NONE;

                static AuthProvider parse(final String alias) {
                    for (final AuthProvider provider : values()) {
                        if (provider.name().equals(alias)) {
                            return provider;
                        }
                    }

                    if (alias == null || alias.isEmpty()) {
                        return NONE;
                    }

                    throw new InvalidConfigurationException("Unknown type of auth provider: " + alias);
                }
            }

            static class Scope {

                private final String realm;
                private final String scheme;
                private final String host;
                private final int port;

                Scope(final String realm, final String scheme, final String host, final int port) {
                    this.realm = realm;
                    this.scheme = scheme;
                    this.host = host;
                    this.port = port;
                }

                public String realm() {
                    return realm;
                }

                public String host() {
                    return host;
                }

                public String scheme() {
                    return scheme;
                }

                public int port() {
                    return port;
                }

                @Override
                public String toString() {
                    return "Scope{"
                            + "realm='" + realm + '\''
                            + ", scheme='" + scheme + '\''
                            + ", host='" + host + '\''
                            + ", port=" + port
                            + '}';
                }
            }

            static class Credentials {

            }

            static class BasicCredentials extends Credentials {

                private final String username;
                private final char[] password;

                BasicCredentials(final String username, final char[] password) {
                    this.username = username;
                    this.password = password;
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

        static class TLSConfiguration {

            protected final String protocol;
            protected final KStore trustStore;

            TLSConfiguration(final String protocol, final KStore trustStore) {
                this.protocol = protocol;
                this.trustStore = trustStore;
            }

            SSLContext createSSLContext() throws GeneralSecurityException, IOException {
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

            static class KStore {

                private final String type;
                private final String location;
                private final char[] password;

                KStore(final String type, final String location, final char[] password) {
                    this.type = type;
                    this.location = location;
                    this.password = password;
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

            private final KStore keyStore;
            private final char[] keyPassword;

            MutualTLSConfiguration(
                    final String protocol,
                    final KStore trustStore,
                    final KStore keyStore,
                    final char[] keyPassword) {
                super(protocol, trustStore);
                this.keyStore = keyStore;
                this.keyPassword = keyPassword;
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
