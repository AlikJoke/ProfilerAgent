package ru.joke.profiler.core.output.handlers.jdbc;

import ru.joke.profiler.core.output.handlers.OutputData;
import ru.joke.profiler.core.output.handlers.OutputDataSink;

// TODO
/**
 * db type +
 * db name +
 * server host +
 * server port +
 * connection properties -
 * auto create table -
 * table name -
 * columns to store (metadata: key -> column) -
 *
 * connection pool: max-pool-size, initial-pool-size, enable-batching, keep-alive-connections-time,
 * batch-size, batch-flush-interval, async-inserts
 *
 */
public class OutputDataJdbcSink implements OutputDataSink<OutputData> {
    @Override
    public void init() {

    }

    @Override
    public void write(OutputData outputData) {

    }

    @Override
    public void close() {
        OutputDataSink.super.close();
    }
}
