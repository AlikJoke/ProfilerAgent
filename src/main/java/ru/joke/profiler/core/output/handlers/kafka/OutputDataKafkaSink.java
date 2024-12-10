package ru.joke.profiler.core.output.handlers.kafka;

import ru.joke.profiler.core.output.handlers.OutputData;
import ru.joke.profiler.core.output.handlers.OutputDataSink;

// TODO
public class OutputDataKafkaSink implements OutputDataSink {
    @Override
    public void init() {
        OutputDataSink.super.init();
    }

    @Override
    public void write(OutputData outputData) {

    }

    @Override
    public void close() {
        OutputDataSink.super.close();
    }
}
