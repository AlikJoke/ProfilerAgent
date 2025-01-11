package ru.joke.profiler.output.sinks.fs.jul;

import ru.joke.profiler.output.sinks.OutputDataSink;

import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

final class OutputDataLoggerSink extends OutputDataSink<String> {

    private final Logger logger;
    private final Level level;

    OutputDataLoggerSink(final LoggerSinkConfiguration configuration) {
        checkNotNull(configuration, "configuration");
        this.level = Level.parse(configuration.level());
        this.logger = Logger.getLogger(configuration.category());
    }

    @Override
    public void write(final String outputData) {
        this.logger.log(this.level, outputData);
    }
}
