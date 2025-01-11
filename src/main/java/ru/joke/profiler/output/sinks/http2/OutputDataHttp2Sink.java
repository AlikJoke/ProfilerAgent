package ru.joke.profiler.output.sinks.http2;

import ru.joke.profiler.output.sinks.OutputData;
import ru.joke.profiler.output.sinks.OutputDataSink;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

final class OutputDataHttp2Sink extends OutputDataSink<OutputData> {

    private final Http2MessageChannel messageChannel;

    OutputDataHttp2Sink(final Http2MessageChannel messageChannel) {
        this.messageChannel = checkNotNull(messageChannel, "messageChannel");
    }

    @Override
    public void init() {
        this.logger.info("Http2 sink initializing");
        this.messageChannel.init();
        this.logger.info("Http2 sink initialized");
    }

    @Override
    public void write(final OutputData dataItem) {
        this.messageChannel.send(dataItem);
    }

    @Override
    public synchronized void close() {
        this.logger.info("Sink will be closed");
        this.messageChannel.close();
        this.logger.info("Sink closed");
    }
}
