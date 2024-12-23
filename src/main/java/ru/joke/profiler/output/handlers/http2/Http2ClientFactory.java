package ru.joke.profiler.output.handlers.http2;

import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.LaxRedirectStrategy;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.auth.CredentialsProviderBuilder;
import org.apache.hc.client5.http.impl.auth.SystemDefaultCredentialsProvider;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.ssl.H2ClientTlsStrategy;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import ru.joke.profiler.configuration.InvalidConfigurationException;
import ru.joke.profiler.output.handlers.OutputDataSink;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

final class Http2ClientFactory {

    private static final Logger logger = Logger.getLogger(OutputDataSink.class.getCanonicalName());

    private static final String USER_AGENT = "JokeProfiler";
    private static final String HTTP2_CLIENT_THREAD_NAME = "profiler-http2-thread-";

    private final Http2SinkConfiguration.Http2ClientConfiguration configuration;

    Http2ClientFactory(final Http2SinkConfiguration.Http2ClientConfiguration configuration) {
        this.configuration = configuration;
    }

    Http2Client create() {

        final Http2SinkConfiguration.Http2ClientConfiguration.ConnectionConfiguration connectionConfiguration = configuration.connectionConfiguration();
        final Http2SinkConfiguration.Http2ClientConfiguration.RequestConfiguration requestConfiguration = configuration.requestConfiguration();
        final Http2SinkConfiguration.Http2ClientConfiguration.IOConfiguration ioConfiguration = configuration.ioConfiguration();

        final HttpRequestRetryStrategy retryStrategy = new DefaultHttpRequestRetryStrategy(requestConfiguration.maxRetries(), TimeValue.ofMilliseconds(requestConfiguration.retriesIntervalMs()));
        final CredentialsProvider credentialsProvider = createCredentialsProvider(configuration.authenticationConfiguration());

        final CloseableHttpAsyncClient client =
                HttpAsyncClients
                        .customHttp2()
                            .evictIdleConnections(TimeValue.ofMilliseconds(connectionConfiguration.idleConnectionTimeoutMs()))
                            .setDefaultCookieStore(new BasicCookieStore())
                            .setUserAgent(USER_AGENT)
                            .setTlsStrategy(buildTlsStrategy(configuration))
                            .setThreadFactory(createHttp2ClientThreadFactory())
                            .setRetryStrategy(retryStrategy)
                            .setRedirectStrategy(LaxRedirectStrategy.INSTANCE)
                            .setIoReactorExceptionCallback(e -> logger.log(Level.SEVERE, "Encountered error in IO reactor", e))
                            .setDefaultCredentialsProvider(credentialsProvider)
                            .useSystemProperties()
                            .setH2Config(
                                    H2Config.initial()
                                                .setMaxFrameSize(requestConfiguration.maxFrameSize())
                                                .setInitialWindowSize(requestConfiguration.initialWindowSize())
                                                .setMaxConcurrentStreams(requestConfiguration.maxConcurrentStreams())
                                                .setPushEnabled(false)
                                                .setCompressionEnabled(!requestConfiguration.compressionDisabled())
                                            .build()
                            )
                            .setIOReactorConfig(IOReactorConfig.custom()
                                    .setIoThreadCount(ioConfiguration.threadCount())
                                    .setSndBufSize(ioConfiguration.sendBufferSize())
                                    .setSoKeepAlive(true)
                                    .setSoLinger(TimeValue.ofMilliseconds(ioConfiguration.lingerTimeoutMs()))
                                    .setTcpNoDelay(ioConfiguration.tcpNoDelay())
                                    .setTcpKeepInterval((int) TimeUnit.MILLISECONDS.toSeconds(ioConfiguration.keepAliveIntervalMs()))
                                    .setTcpKeepIdle((int) TimeUnit.MILLISECONDS.toSeconds(ioConfiguration.idleTimeoutMs()))
                                    .setTcpKeepCount(ioConfiguration.maxKeepAliveProbesBeforeDrop())
                                    .setSocksProxyAddress(ioConfiguration.socksProxyHost() == null ? null : new InetSocketAddress(ioConfiguration.socksProxyHost(), ioConfiguration.socksProxyPort()))
                                    .setSocksProxyUsername(ioConfiguration.socksProxyUsername())
                                    .setSocksProxyPassword(new String(ioConfiguration.socksProxyPassword()))
                                    .setSoTimeout(Timeout.ofMilliseconds(ioConfiguration.ioTimeoutMs()))
                                    .build())
                            .setDefaultConnectionConfig(
                                    ConnectionConfig.custom()
                                                        .setValidateAfterInactivity(Timeout.ofMilliseconds(connectionConfiguration.validateAfterInactivityIntervalMs()))
                                                        .setSocketTimeout(Timeout.ofMilliseconds(connectionConfiguration.socketTimeoutMs()))
                                                        .setConnectTimeout(Timeout.ofMilliseconds(connectionConfiguration.connectTimeoutMs()))
                                                        .setTimeToLive(Timeout.ofMilliseconds(connectionConfiguration.connectionTimeToLiveMs()))
                                                    .build()
                            )
                            .setDefaultRequestConfig(
                                    RequestConfig.custom()
                                                    .setAuthenticationEnabled(requestConfiguration.authenticationEnabled())
                                                    .setCircularRedirectsAllowed(requestConfiguration.circularRedirectsAllowed())
                                                    .setConnectionKeepAlive(TimeValue.ofMilliseconds(requestConfiguration.keepAliveMs()))
                                                    .setConnectionRequestTimeout(Timeout.ofMilliseconds(requestConfiguration.connectionManagerRequestTimeoutMs()))
                                                    .setContentCompressionEnabled(!requestConfiguration.disableContentCompression())
                                                    .setExpectContinueEnabled(requestConfiguration.expectContinueEnabled())
                                                    .setMaxRedirects(requestConfiguration.maxRedirects())
                                                    .setProtocolUpgradeEnabled(!requestConfiguration.disableProtocolUpgrade())
                                                    .setRedirectsEnabled(!requestConfiguration.disableRedirects())
                                                    .setCookieSpec(StandardCookieSpec.STRICT)
                                                .build()
                            )
                .build();

        return new Http2Client(client, configuration);
    }

    private TlsStrategy buildTlsStrategy(final Http2SinkConfiguration.Http2ClientConfiguration clientConfiguration) {
        final Http2SinkConfiguration.Http2ClientConfiguration.TLSConfiguration tlsConfiguration = clientConfiguration.tlsConfiguration();
        if (tlsConfiguration == null) {
            return null;
        }

        try {
            final SSLContext sslContext = tlsConfiguration.createSSLContext();
            return new H2ClientTlsStrategy(sslContext);
        } catch (GeneralSecurityException e) {
            throw new InvalidConfigurationException("Invalid key store or trust store provided", e);
        } catch (IOException e) {
            throw new InvalidConfigurationException("Unable to open key store or trust store", e);
        }
    }

    private ThreadFactory createHttp2ClientThreadFactory() {
        final AtomicInteger counter = new AtomicInteger();
        return r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(HTTP2_CLIENT_THREAD_NAME + counter.getAndIncrement());
            thread.setUncaughtExceptionHandler((t, e) -> logger.log(Level.SEVERE, "Exception in profiler http2 client thread", e));

            return thread;
        };
    }

    private CredentialsProvider createCredentialsProvider(final Http2SinkConfiguration.Http2ClientConfiguration.AuthenticationConfiguration configuration) {
        switch (configuration.authProvider()) {
            case SYSTEM_DEFAULT:
                return new SystemDefaultCredentialsProvider();
            case BASIC:
                final Http2SinkConfiguration.Http2ClientConfiguration.AuthenticationConfiguration.Scope authScope = configuration.scope();
                final HttpHost host = new HttpHost(authScope.host(), authScope.port());
                final AuthScope scope = new AuthScope(host, authScope.realm(), null);
                final Http2SinkConfiguration.Http2ClientConfiguration.AuthenticationConfiguration.BasicCredentials credentials = (Http2SinkConfiguration.Http2ClientConfiguration.AuthenticationConfiguration.BasicCredentials) configuration.credentials();
                return CredentialsProviderBuilder
                        .create()
                            .add(scope, credentials.username(), credentials.password())
                        .build();
            default:
                return null;
        }
    }
}
