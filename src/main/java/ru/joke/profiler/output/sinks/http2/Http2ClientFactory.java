package ru.joke.profiler.output.sinks.http2;

import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.LaxRedirectStrategy;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.ssl.H2ClientTlsStrategy;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import ru.joke.profiler.configuration.InvalidConfigurationException;
import ru.joke.profiler.util.ProfilerThreadFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

final class Http2ClientFactory {

    private static final Logger logger = Logger.getLogger(Http2ClientFactory.class.getCanonicalName());

    private static final String USER_AGENT = "JokeProfiler";
    private static final String HTTP2_CLIENT_THREAD_NAME = "profiler-http2-thread-";

    private final Http2SinkConfiguration.Http2ClientConfiguration configuration;
    private final Http2ClientCredentialsProviderFactory credentialsProviderFactory;

    Http2ClientFactory(
            final Http2SinkConfiguration.Http2ClientConfiguration configuration,
            final Http2ClientCredentialsProviderFactory credentialsProviderFactory
    ) {
        this.configuration = configuration;
        this.credentialsProviderFactory = credentialsProviderFactory;
    }

    Http2Client create() {

        final Http2SinkConfiguration.Http2ClientConfiguration.ConnectionConfiguration connectionConfiguration = configuration.connectionConfiguration();
        final Http2SinkConfiguration.Http2ClientConfiguration.RequestConfiguration requestConfiguration = configuration.requestConfiguration();
        final Http2SinkConfiguration.Http2ClientConfiguration.IOConfiguration ioConfiguration = configuration.ioConfiguration();

        final HttpRequestRetryStrategy retryStrategy = new DefaultHttpRequestRetryStrategy(requestConfiguration.maxRetries(), TimeValue.ofMilliseconds(requestConfiguration.retriesIntervalMs()));
        final CredentialsProvider credentialsProvider = this.credentialsProviderFactory.create();

        final CloseableHttpAsyncClient client =
                HttpAsyncClients
                        .customHttp2()
                            .evictIdleConnections(TimeValue.ofMilliseconds(connectionConfiguration.idleConnectionTimeoutMs()))
                            .setDefaultCookieStore(new BasicCookieStore())
                            .setUserAgent(USER_AGENT)
                            .setTlsStrategy(buildTlsStrategy(configuration))
                            .setThreadFactory(new ProfilerThreadFactory(HTTP2_CLIENT_THREAD_NAME, true))
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
                                                .setCompressionEnabled(requestConfiguration.compressionEnabled())
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
                                                        .setValidateAfterInactivity(createTimeout(connectionConfiguration.validateAfterInactivityIntervalMs()))
                                                        .setSocketTimeout(createTimeout(connectionConfiguration.socketTimeoutMs()))
                                                        .setConnectTimeout(createTimeout(connectionConfiguration.connectTimeoutMs()))
                                                        .setTimeToLive(createTimeout(connectionConfiguration.connectionTimeToLiveMs()))
                                                    .build()
                            )
                            .setDefaultRequestConfig(
                                    RequestConfig.custom()
                                                    .setAuthenticationEnabled(requestConfiguration.authenticationEnabled())
                                                    .setCircularRedirectsAllowed(requestConfiguration.circularRedirectsAllowed())
                                                    .setConnectionKeepAlive(TimeValue.ofMilliseconds(requestConfiguration.keepAliveMs()))
                                                    .setConnectionRequestTimeout(Timeout.ofMilliseconds(requestConfiguration.connectionManagerRequestTimeoutMs()))
                                                    .setContentCompressionEnabled(requestConfiguration.compressionEnabled())
                                                    .setExpectContinueEnabled(requestConfiguration.expectContinueEnabled())
                                                    .setMaxRedirects(requestConfiguration.maxRedirects())
                                                    .setProtocolUpgradeEnabled(!requestConfiguration.disableProtocolUpgrade())
                                                    .setRedirectsEnabled(requestConfiguration.maxRedirects() > 0)
                                                    .setCookieSpec(StandardCookieSpec.STRICT)
                                                .build()
                            )
                .build();

        return new Http2Client(client, configuration);
    }

    private Timeout createTimeout(final long timeoutMs) {
        return timeoutMs == -1 ? null : Timeout.ofMilliseconds(timeoutMs);
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
}
