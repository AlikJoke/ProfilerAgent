package ru.joke.profiler.output.sinks.jms;

import jakarta.jms.JMSProducer;
import ru.joke.profiler.output.sinks.util.injectors.OutputPropertiesInjector;

import java.time.LocalDateTime;
import java.util.Map;

final class JmsProducerPropertiesInjector extends OutputPropertiesInjector<JMSProducer> {

    private final Map<String, String> propertiesMapping;

    JmsProducerPropertiesInjector(final Map<String, String> propertiesMapping) {
        super(propertiesMapping.keySet());
        this.propertiesMapping = propertiesMapping;
    }

    @Override
    protected JMSProducer injectMethodName(
            final JMSProducer template,
            final String method,
            final String property,
            final int propertyIndex
    ) {
        return addStringProperty(template, property, method);
    }

    @Override
    protected JMSProducer injectMethodEnterTimestamp(
            final JMSProducer template,
            final long methodEnterTimestamp,
            final String property,
            final int propertyIndex
    ) {
        return addLongProperty(template, property, methodEnterTimestamp);
    }

    @Override
    protected JMSProducer injectMethodElapsedTime(
            final JMSProducer template,
            final long methodElapsedTime,
            final String property,
            final int propertyIndex
    ) {
        return addLongProperty(template, property, methodElapsedTime);
    }

    @Override
    protected JMSProducer injectTraceId(
            final JMSProducer template,
            final String traceId,
            final String property,
            final int propertyIndex
    ) {
        return addStringProperty(template, property, traceId);
    }

    @Override
    protected JMSProducer injectDepth(
            final JMSProducer template,
            final int depth,
            final String property,
            final int propertyIndex
    ) {
        return template.setProperty(this.propertiesMapping.getOrDefault(property, property), depth);
    }

    @Override
    protected JMSProducer injectIp(
            final JMSProducer template,
            final String ip,
            final String property,
            final int propertyIndex
    ) {
        return addStringProperty(template, property, ip);
    }

    @Override
    protected JMSProducer injectHost(
            final JMSProducer template,
            final String host,
            final String property,
            final int propertyIndex
    ) {
        return addStringProperty(template, property, host);
    }

    @Override
    protected JMSProducer injectSource(
            final JMSProducer template,
            final String source,
            final String property,
            final int propertyIndex
    ) {
        return addStringProperty(template, property, source);
    }

    @Override
    protected JMSProducer injectSystemProperty(
            final JMSProducer template,
            final String systemPropertyValue,
            final String property,
            final int propertyIndex
    ) {
        return addStringProperty(template, property, systemPropertyValue);
    }

    @Override
    protected JMSProducer injectThreadName(
            final JMSProducer template,
            final String threadName,
            final String property,
            final int propertyIndex
    ) {
        return addStringProperty(template, property, threadName);
    }

    @Override
    protected JMSProducer injectCurrentTimestamp(
            final JMSProducer template,
            final LocalDateTime timestamp,
            final String property,
            final int propertyIndex
    ) {
        return addStringProperty(template, property, timestamp.toString());
    }

    private JMSProducer addStringProperty(
            final JMSProducer producer,
            final String property,
            final String value
    ) {
        final String headerName = this.propertiesMapping.getOrDefault(property, property);
        return producer.setProperty(headerName, value);
    }

    private JMSProducer addLongProperty(
            final JMSProducer producer,
            final String property,
            final long value
    ) {
        final String headerName = this.propertiesMapping.getOrDefault(property, property);
        return producer.setProperty(headerName, value);
    }
}
