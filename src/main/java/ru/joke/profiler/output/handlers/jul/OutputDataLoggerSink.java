package ru.joke.profiler.output.handlers.jul;

import ru.joke.profiler.output.handlers.OutputDataSink;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class OutputDataLoggerSink implements OutputDataSink<String> {

    private final Logger logger;
    private final Level level;

    public OutputDataLoggerSink(final String category, final String level) {
        this.level = Level.parse(level);
        this.logger = Logger.getLogger(category);
    }

    @Override
    public void write(final String outputData) {
        this.logger.log(this.level, outputData);
    }
}
