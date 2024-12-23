package ru.joke.profiler.output.handlers.kafka;

import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.OutputDataSink;

final class OutputDataKafkaSink implements OutputDataSink<OutputData> {

    private final KafkaMessageChannel channel;

    OutputDataKafkaSink(final KafkaMessageChannel channel) {
        this.channel = channel;
    }

    @Override
    public void init() {
        this.channel.init();
    }

    @Override
    public void write(final OutputData outputData) {
        this.channel.send(outputData);
    }

    @Override
    public synchronized void close() {
        this.channel.close();
    }
}
