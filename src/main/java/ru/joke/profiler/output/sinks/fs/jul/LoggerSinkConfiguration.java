package ru.joke.profiler.output.sinks.fs.jul;

import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.output.sinks.async.AsyncSinkDataFlushingConfiguration;
import ru.joke.profiler.output.sinks.fs.AbstractFsSinkConfiguration;

import static ru.joke.profiler.output.sinks.fs.jul.OutputDataLoggerSinkHandle.SINK_TYPE;
import static ru.joke.profiler.util.ArgUtil.checkNotEmpty;

public final class LoggerSinkConfiguration extends AbstractFsSinkConfiguration {

    private static final String LOGGER_SINK_PROPERTIES_PREFIX = SINK_TYPE + ".";

    private static final String OUTPUT_CATEGORY = "output_category";
    private static final String OUTPUT_LEVEL = "output_level";

    private final String category;
    private final String level;

    @ProfilerConfigurationPropertiesWrapper(prefix = LOGGER_SINK_PROPERTIES_PREFIX)
    public LoggerSinkConfiguration(
            @ProfilerConfigurationProperty(name = OUTPUT_DATA_PATTERN) final String outputDataPattern,
            @ProfilerConfigurationProperty(name = OUTPUT_CATEGORY, required = true) final String category,
            @ProfilerConfigurationProperty(name = OUTPUT_LEVEL, required = true) final String level,
            final AsyncSinkDataFlushingConfiguration asyncFlushingConfiguration
    ) {
        super(outputDataPattern, asyncFlushingConfiguration);
        this.category = checkNotEmpty(category, "category");
        this.level = checkNotEmpty(level, "level");
    }

    public String category() {
        return category;
    }

    public String level() {
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
