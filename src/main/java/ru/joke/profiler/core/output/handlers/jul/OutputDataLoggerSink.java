package ru.joke.profiler.core.output.handlers.jul;

import ru.joke.profiler.core.output.handlers.OutputData;
import ru.joke.profiler.core.output.handlers.OutputDataSink;
import ru.joke.profiler.core.output.handlers.OutputStringDataFormatter;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class OutputDataLoggerSink implements OutputDataSink {

    private final Logger logger;
    private final Level level;
    private final OutputStringDataFormatter formatter;

    public OutputDataLoggerSink(
            final OutputStringDataFormatter formatter,
            final String category,
            final String level) {
        this.formatter = formatter;
        this.level = Level.parse(level);
        this.logger = Logger.getLogger(category);
    }

    @Override
    public void write(final OutputData outputData) {
        this.logger.log(this.level, () -> this.formatter.format(outputData));
    }
}
