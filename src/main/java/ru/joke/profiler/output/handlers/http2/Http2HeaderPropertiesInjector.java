package ru.joke.profiler.output.handlers.http2;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import ru.joke.profiler.output.handlers.util.OutputPropertiesInjector;

import java.time.LocalDateTime;
import java.util.Map;

final class Http2HeaderPropertiesInjector extends OutputPropertiesInjector<Header[]> {
    
    private final Map<String, String> properties2headersMapping;

    Http2HeaderPropertiesInjector(final Map<String, String> properties2headersMapping) {
        super(properties2headersMapping.keySet());
        this.properties2headersMapping = properties2headersMapping;
    }

    @Override
    protected Header[] injectMethodName(
            final Header[] template,
            final String method,
            final String property,
            final int propertyIndex
    ) {
        return addHeader(template, property, method, propertyIndex);
    }

    @Override
    protected Header[] injectMethodEnterTimestamp(
            final Header[] template,
            final long methodEnterTimestamp,
            final String property,
            final int propertyIndex
    ) {
        return addHeader(template, property, methodEnterTimestamp, propertyIndex);
    }

    @Override
    protected Header[] injectMethodElapsedTime(
            final Header[] template,
            final long methodElapsedTime,
            final String property,
            final int propertyIndex
    ) {
        return addHeader(template, property, methodElapsedTime, propertyIndex);
    }

    @Override
    protected Header[] injectTraceId(
            final Header[] template,
            final String traceId,
            final String property,
            final int propertyIndex
    ) {
        return addHeader(template, property, traceId, propertyIndex);
    }

    @Override
    protected Header[] injectDepth(
            final Header[] template,
            final int depth,
            final String property,
            final int propertyIndex
    ) {
        return addHeader(template, property, depth, propertyIndex);
    }

    @Override
    protected Header[] injectIp(
            final Header[] template,
            final String ip,
            final String property,
            final int propertyIndex
    ) {
        return addHeader(template, property, ip, propertyIndex);
    }

    @Override
    protected Header[] injectHost(
            final Header[] template,
            final String host,
            final String property,
            final int propertyIndex
    ) {
        return addHeader(template, property, host, propertyIndex);
    }

    @Override
    protected Header[] injectSource(
            final Header[] template,
            final String source,
            final String property,
            final int propertyIndex
    ) {
        return addHeader(template, property, source, propertyIndex);
    }

    @Override
    protected Header[] injectSystemProperty(
            final Header[] template,
            final String systemPropertyValue,
            final String property,
            final int propertyIndex
    ) {
        return addHeader(template, property, systemPropertyValue, propertyIndex);
    }

    @Override
    protected Header[] injectThreadName(
            final Header[] template,
            final String threadName,
            final String property,
            final int propertyIndex
    ) {
        return addHeader(template, property, threadName, propertyIndex);
    }

    @Override
    protected Header[] injectCurrentTimestamp(
            final Header[] template,
            final LocalDateTime timestamp,
            final String property,
            final int propertyIndex
    ) {
        return addHeader(template, property, timestamp.toString(), propertyIndex);
    }
    
    private Header[] addHeader(
            final Header[] headers, 
            final String property, 
            final Object value,
            final int propertyIndex
    ) {
        final String headerName = this.properties2headersMapping.getOrDefault(property, property);
        final Header header = new BasicHeader(headerName, value, true);
        headers[propertyIndex - 1] = header;
        return headers;
    }
}
