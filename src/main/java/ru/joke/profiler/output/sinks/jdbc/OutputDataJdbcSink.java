package ru.joke.profiler.output.sinks.jdbc;

import ru.joke.profiler.output.sinks.OutputData;
import ru.joke.profiler.output.sinks.OutputDataSink;
import ru.joke.profiler.output.sinks.ProfilerOutputSinkException;

import java.sql.SQLException;
import java.util.List;

final class OutputDataJdbcSink implements OutputDataSink<OutputData> {

    private final OutputDataJdbcStorage storage;
    private final OutputDataTablePreparer outputDataTablePreparer;

    OutputDataJdbcSink(
            final OutputDataJdbcStorage storage,
            final OutputDataTablePreparer outputDataTablePreparer
    ) {
        this.storage = storage;
        this.outputDataTablePreparer = outputDataTablePreparer;
    }

    @Override
    public void init() {
        try {
            this.outputDataTablePreparer.prepare();
            this.storage.init();
        } catch (SQLException ex) {
            throw new ProfilerOutputSinkException(ex);
        }
    }

    @Override
    public void write(final OutputData dataItem) {
        this.storage.store(dataItem);
    }

    @Override
    public void write(final List<OutputData> dataItems) {
        this.storage.store(dataItems);
    }

    @Override
    public void close() {
        this.storage.close();
    }
}
