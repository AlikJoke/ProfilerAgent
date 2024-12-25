package ru.joke.profiler.output.handlers.http2;

import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.OutputDataSink;

final class OutputDataHttp2Sink implements OutputDataSink<OutputData> {

    private final Http2MessageChannel messageChannel;

    OutputDataHttp2Sink(final Http2MessageChannel messageChannel) {
        this.messageChannel = messageChannel;
    }

    @Override
    public void init() {
        this.messageChannel.init();
    }

    @Override
    public void write(final OutputData dataItem) {
        this.messageChannel.send(dataItem);
    }

    @Override
    public void close() {
        this.messageChannel.close();
    }
}
