package ru.joke.profiler.output.handlers.jms;

import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.OutputDataSink;

import java.util.List;

final class OutputDataJmsSink implements OutputDataSink<OutputData> {

    private final JmsMessageChannel messageChannel;

    OutputDataJmsSink(final JmsMessageChannel messageChannel) {
        this.messageChannel = messageChannel;
    }

    @Override
    public void init() {
        this.messageChannel.init();
    }

    @Override
    public void write(final OutputData outputData) {
        this.messageChannel.send(outputData);
    }

    @Override
    public void write(final List<OutputData> outputData) {
        this.messageChannel.send(outputData);
    }

    @Override
    public void close() {
        this.messageChannel.close();
    }
}
