package ru.joke.profiler.output.handlers.fs.jul;

import ru.joke.profiler.output.handlers.OutputDataSink;

import java.util.logging.Level;
import java.util.logging.Logger;

final class OutputDataLoggerSink implements OutputDataSink<String> {

    private final Logger logger;
    private final Level level;

    OutputDataLoggerSink(final LoggerSinkConfiguration configuration) {
        this.level = Level.parse(configuration.level());
        this.logger = Logger.getLogger(configuration.category());
    }

    @Override
    public void write(final String outputData) {
        this.logger.log(this.level, outputData);
    }
}
