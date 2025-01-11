package ru.joke.profiler.output.sinks.kafka;

import ru.joke.profiler.output.sinks.OutputData;
import ru.joke.profiler.output.sinks.OutputDataSink;

final class OutputDataKafkaSink extends OutputDataSink<OutputData> {

    private final KafkaMessageChannel channel;

    OutputDataKafkaSink(final KafkaMessageChannel channel) {
        this.channel = channel;
    }

    @Override
    public void init() {
        logger.info("Kafka sink will be initialized");
        this.channel.init();
        logger.info("Kafka sink initialized");
    }

    @Override
    public void write(final OutputData outputData) {
        this.channel.send(outputData);
    }

    @Override
    public synchronized void close() {
        logger.info("Kafka sink will be closed");
        this.channel.close();
        logger.info("Kafka sink closed");
    }
}
