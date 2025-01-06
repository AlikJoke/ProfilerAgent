package ru.joke.profiler.output.sinks;

import java.util.Map;

public interface OutputDataSinkHandle {

    String type();

    OutputDataSink<OutputData> create(Map<String, String> properties) throws Exception;
}
