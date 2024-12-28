package ru.joke.profiler.output.handlers.fs.jul;

import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.output.handlers.fs.AbstractFsSinkConfiguration;

import static ru.joke.profiler.output.handlers.fs.jul.OutputDataLoggerSinkHandle.SINK_TYPE;

public final class LoggerSinkConfiguration extends AbstractFsSinkConfiguration {

    private static final String LOGGER_SINK_PROPERTIES_PREFIX = SINK_TYPE + ".";

    private static final String OUTPUT_CATEGORY = "output_category";
    private static final String OUTPUT_LEVEL = "output_level";

    private final String category;
    private final String level;

    @ProfilerConfigurationPropertiesWrapper(prefix = LOGGER_SINK_PROPERTIES_PREFIX)
    LoggerSinkConfiguration(
            @ProfilerConfigurationProperty(name = OUTPUT_DATA_PATTERN) final String outputDataPattern,
            @ProfilerConfigurationProperty(name = OUTPUT_CATEGORY, required = true) final String category,
            @ProfilerConfigurationProperty(name = OUTPUT_LEVEL, required = true) final String level
    ) {
        super(outputDataPattern);
        this.category = category;
        this.level = level;
    }

    String category() {
        return category;
    }

    String level() {
        return level;
    }

    @Override
    public String toString() {
        return "LoggerSinkConfiguration{"
                + "outputDataPattern='" + outputDataPattern + '\''
                + ", category='" + category + '\''
                + ", level='" + level + '\''
                + '}';
    }
}
