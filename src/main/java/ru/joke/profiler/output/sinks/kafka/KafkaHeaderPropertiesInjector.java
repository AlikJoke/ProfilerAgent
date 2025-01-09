package ru.joke.profiler.output.sinks.kafka;

import org.apache.kafka.common.header.Headers;
import ru.joke.profiler.output.sinks.util.injectors.OutputPropertiesInjector;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

final class KafkaHeaderPropertiesInjector extends OutputPropertiesInjector<Headers> {
    
    private final Map<String, String> properties2headersMapping;

    KafkaHeaderPropertiesInjector(final Map<String, String> properties2headersMapping) {
        super(properties2headersMapping.keySet());
        this.properties2headersMapping = properties2headersMapping;
    }

    @Override
    protected Headers injectMethodName(
            final Headers template,
            final String method,
            final String property,
            final int propertyIndex
    ) {
        return addStringHeader(template, property, method);
    }

    @Override
    protected Headers injectMethodEnterTimestamp(
            final Headers template,
            final long methodEnterTimestamp,
            final String property,
            final int propertyIndex
    ) {
        return addLongHeader(template, property, methodEnterTimestamp);
    }

    @Override
    protected Headers injectMethodElapsedTime(
            final Headers template,
            final long methodElapsedTime,
            final String property,
            final int propertyIndex
    ) {
        return addLongHeader(template, property, methodElapsedTime);
    }

    @Override
    protected Headers injectTraceId(
            final Headers template,
            final String traceId,
            final String property,
            final int propertyIndex
    ) {
        return addStringHeader(template, property, traceId);
    }

    @Override
    protected Headers injectDepth(
            final Headers template,
            final int depth,
            final String property,
            final int propertyIndex
    ) {
        return addIntHeader(template, property, depth);
    }

    @Override
    protected Headers injectSpanId(
            final Headers template,
            final String spanId,
            final String property,
            final int propertyIndex
    ) {
        return addStringHeader(template, property, spanId);
    }

    @Override
    protected Headers injectParentSpanId(
            final Headers template,
            final String parentSpanId,
            final String property,
            final int propertyIndex
    ) {
        return addStringHeader(template, property, parentSpanId);
    }

    @Override
    protected Headers injectIp(
            final Headers template,
            final String ip,
            final String property,
            final int propertyIndex
    ) {
        return addStringHeader(template, property, ip);
    }

    @Override
    protected Headers injectHost(
            final Headers template,
            final String host,
            final String property,
            final int propertyIndex
    ) {
        return addStringHeader(template, property, host);
    }

    @Override
    protected Headers injectSource(
            final Headers template,
            final String source,
            final String property,
            final int propertyIndex
    ) {
        return addStringHeader(template, property, source);
    }

    @Override
    protected Headers injectSystemProperty(
            final Headers template,
            final String systemPropertyValue,
            final String property,
            final int propertyIndex
    ) {
        return addStringHeader(template, property, systemPropertyValue);
    }

    @Override
    protected Headers injectThreadName(
            final Headers template,
            final String threadName,
            final String property,
            final int propertyIndex
    ) {
        return addStringHeader(template, property, threadName);
    }

    @Override
    protected Headers injectCurrentTimestamp(
            final Headers template,
            final LocalDateTime timestamp,
            final String property,
            final int propertyIndex
    ) {
        return addStringHeader(template, property, timestamp.toString());
    }
    
    private Headers addStringHeader(
            final Headers headers, 
            final String property, 
            final String value
    ) {
        return headers.add(this.properties2headersMapping.getOrDefault(property, property), value.getBytes(StandardCharsets.UTF_8));
    }

    private Headers addLongHeader(
            final Headers headers,
            final String property,
            final long value
    ) {
        return headers.add(this.properties2headersMapping.getOrDefault(property, property), longToByteArray(value));
    }

    private Headers addIntHeader(
            final Headers headers,
            final String property,
            final int value
    ) {
        return headers.add(this.properties2headersMapping.getOrDefault(property, property), intToByteArray(value));
    }

    private byte[] intToByteArray(final int value) {
        final byte[] result = new byte[Integer.BYTES];
        final int length = result.length;
        for (int i = 0; i < length; i++) {
            result[length - i - 1] = (byte) ((value >> 8 * i) & 0xFF);
        }

        return result;
    }

    private byte[] longToByteArray(final long value) {
        final byte[] result = new byte[Long.BYTES];
        final int length = result.length;
        for (int i = 0; i < length; i++) {
            result[length - i - 1] = (byte) ((value >> 8 * i) & 0xFF);
        }

        return result;
    }
}
