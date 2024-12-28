package ru.joke.profiler.output.handlers.fs.stream.file;

import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.output.handlers.fs.stream.AbstractFsStreamSinkConfiguration;

import static ru.joke.profiler.output.handlers.fs.stream.file.OutputDataFileSinkHandle.SINK_TYPE;

public final class FileSinkConfiguration extends AbstractFsStreamSinkConfiguration {

    private static final String FILE_SINK_PROPERTIES_PREFIX = SINK_TYPE + ".";

    private static final String OUTPUT_FILE_PATH = "output_file";
    private static final String EXISTING_OUTPUT_FILE_POLICY = "existing_output_file_policy";

    private final String filePath;
    private final ExistingFilePolicy existingFilePolicy;

    @ProfilerConfigurationPropertiesWrapper(prefix = FILE_SINK_PROPERTIES_PREFIX)
    FileSinkConfiguration(
            @ProfilerConfigurationProperty(name = OUTPUT_DATA_PATTERN) final String outputDataPattern,
            @ProfilerConfigurationProperty(name = OUTPUT_STREAM_BUFFER_SIZE, defaultValue = "8192") final int bufferSize,
            @ProfilerConfigurationProperty(name = OUTPUT_STREAM_FORCE_FLUSH_ON_WRITES) final boolean forceFlushOnWrites,
            @ProfilerConfigurationProperty(name = OUTPUT_FILE_PATH, required = true) final String filePath,
            @ProfilerConfigurationProperty(name = EXISTING_OUTPUT_FILE_POLICY) final ExistingFilePolicy existingFilePolicy
    ) {
        super(outputDataPattern, bufferSize, forceFlushOnWrites);
        this.filePath = filePath;
        this.existingFilePolicy = existingFilePolicy;
    }

    String filePath() {
        return filePath;
    }

    ExistingFilePolicy existingFilePolicy() {
        return existingFilePolicy;
    }

    @Override
    public String toString() {
        return "FileSinkConfiguration{"
                + "filePath='" + filePath + '\''
                + ", existingFilePolicy=" + existingFilePolicy
                + ", bufferSize=" + bufferSize
                + ", forceFlushOnWrites=" + forceFlushOnWrites
                + ", outputDataPattern='" + outputDataPattern + '\''
                + '}';
    }
}
