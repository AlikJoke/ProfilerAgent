package ru.joke.profiler.output.sinks.jms;

import ru.joke.profiler.output.sinks.OutputData;
import ru.joke.profiler.output.sinks.OutputDataSink;

import java.util.List;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

final class OutputDataJmsSink extends OutputDataSink<OutputData> {

    private final JmsMessageChannel messageChannel;

    OutputDataJmsSink(final JmsMessageChannel messageChannel) {
        this.messageChannel = checkNotNull(messageChannel, "messageChannel");
    }

    @Override
    public void init() {
        logger.info("JMS sink will be initialized");
        this.messageChannel.init();
        logger.info("JMS sink initialized");
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
        logger.info("JMS sink will be closed");
        this.messageChannel.close();
        logger.info("JMS sink closed");
    }
}
