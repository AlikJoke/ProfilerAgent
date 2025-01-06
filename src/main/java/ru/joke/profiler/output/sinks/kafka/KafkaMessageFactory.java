package ru.joke.profiler.output.sinks.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import ru.joke.profiler.output.sinks.OutputData;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

final class KafkaMessageFactory {

    private final KafkaSinkConfiguration.OutputRecordConfiguration configuration;
    private final Function<OutputData, byte[]> bodyConversionFunc;
    private final KafkaHeaderPropertiesInjector headerPropertiesInjector;

    KafkaMessageFactory(
            final KafkaSinkConfiguration.OutputRecordConfiguration configuration,
            final KafkaHeaderPropertiesInjector headerPropertiesInjector,
            final Function<OutputData, byte[]> bodyConversionFunc
    ) {
        this.configuration = configuration;
        this.bodyConversionFunc = bodyConversionFunc;
        this.headerPropertiesInjector = headerPropertiesInjector;
    }

    ProducerRecord<String, byte[]> create(final OutputData data) {
        final byte[] bytesBody = this.bodyConversionFunc.apply(data);
        final Headers headers = createHeaders(data);

        return new ProducerRecord<>(
                this.configuration.outputQueue(),
                null,
                data.traceId(),
                bytesBody,
                headers
        );
    }

    private Headers createHeaders(final OutputData data) {
        return this.headerPropertiesInjector.inject(new RecordHeaders(), data)
                .add(configuration.messageTypeHeader(), configuration.messageType().getBytes(StandardCharsets.UTF_8));
    }
}
