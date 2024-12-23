package ru.joke.profiler.output.handlers.http2;

import org.apache.hc.core5.http.Header;
import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.util.OutputPropertiesInjector;

import java.nio.charset.StandardCharsets;

final class Http2MessageFactory {

    private final Http2SinkConfiguration.OutputMessageConfiguration configuration;
    private final OutputPropertiesInjector<StringBuilder> bodyPropertiesInjector;
    private final OutputPropertiesInjector<Header[]> headersInjector;

    Http2MessageFactory(
            final Http2SinkConfiguration.OutputMessageConfiguration configuration,
            final OutputPropertiesInjector<StringBuilder> bodyPropertiesInjector,
            final OutputPropertiesInjector<Header[]> headersInjector) {
        this.configuration = configuration;
        this.bodyPropertiesInjector = bodyPropertiesInjector;
        this.headersInjector = headersInjector;
    }

    Http2Message create(final OutputData outputData) {

        final StringBuilder bodyBuilder = this.bodyPropertiesInjector.inject(new StringBuilder(), outputData);
        final byte[] body = bodyBuilder.toString().getBytes(StandardCharsets.UTF_8);

        final org.apache.hc.core5.http.Header[] headers = new Header[this.configuration.headersMapping().size()];
        this.headersInjector.inject(headers, outputData);

        return new Http2Message(body, headers);
    }
}
