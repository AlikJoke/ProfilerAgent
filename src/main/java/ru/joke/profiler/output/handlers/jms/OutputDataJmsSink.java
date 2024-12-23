package ru.joke.profiler.output.handlers.jms;

import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.OutputDataSink;

// TODO
public class OutputDataJmsSink implements OutputDataSink<OutputData> {
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
