package ru.joke.profiler.output.handlers.fs.stream.file;

import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.meta.ProfilerDefaultEnumProperty;
import ru.joke.profiler.output.handlers.fs.stream.AbstractFsStreamSinkConfiguration;

import static ru.joke.profiler.output.handlers.fs.stream.file.OutputDataFileSinkHandle.SINK_TYPE;

public final class FileSinkConfiguration extends AbstractFsStreamSinkConfiguration {

    private static final String FILE_SINK_PROPERTIES_PREFIX = SINK_TYPE + ".";

    private static final String OUTPUT_FILE_PATH = "output_file";
    private static final String EXISTING_OUTPUT_FILE_POLICY = "existing_output_file_policy";

    private final String filePath;
    private final ExistingFilePolicy existingFilePolicy;
    private final Rotation rotation;

    @ProfilerConfigurationPropertiesWrapper(prefix = FILE_SINK_PROPERTIES_PREFIX)
    FileSinkConfiguration(
            @ProfilerConfigurationProperty(name = OUTPUT_DATA_PATTERN) final String outputDataPattern,
            @ProfilerConfigurationProperty(name = OUTPUT_STREAM_BUFFER_SIZE, defaultValue = "8192") final int bufferSize,
            @ProfilerConfigurationProperty(name = OUTPUT_STREAM_FORCE_FLUSH_ON_WRITES) final boolean forceFlushOnWrites,
            @ProfilerConfigurationProperty(name = OUTPUT_FILE_PATH, required = true) final String filePath,
            @ProfilerConfigurationProperty(name = EXISTING_OUTPUT_FILE_POLICY) final ExistingFilePolicy existingFilePolicy,
            final Rotation rotation
    ) {
        super(outputDataPattern, bufferSize, forceFlushOnWrites);
        this.filePath = filePath;
        this.existingFilePolicy = existingFilePolicy;
        this.rotation = rotation;
    }

    String filePath() {
        return filePath;
    }

    ExistingFilePolicy existingFilePolicy() {
        return existingFilePolicy;
    }

    Rotation rotation() {
        return rotation;
    }

    @Override
    public String toString() {
        return "FileSinkConfiguration{"
                + "filePath='" + filePath + '\''
                + ", existingFilePolicy=" + existingFilePolicy
                + ", rotation=" + rotation
                + ", bufferSize=" + bufferSize
                + ", forceFlushOnWrites=" + forceFlushOnWrites
                + ", outputDataPattern='" + outputDataPattern + '\''
                + '}';
    }

    static class Rotation {

        private static final String ROTATION_PREFIX = "rotation.";

        private static final String ENABLED = "enabled";
        private static final String OVERFLOW_LIMIT = "overflow_limit_bytes";
        private static final String MAX_ROTATIONS = "max_rotations";
        private static final String ROTATION_MODE = "mode";

        private final boolean enabled;
        private final long overflowLimitBytes;
        private final int maxRotations;
        private final RotationMode mode;

        @ProfilerConfigurationPropertiesWrapper(prefix = ROTATION_PREFIX)
        Rotation(
                @ProfilerConfigurationProperty(name = ENABLED) final boolean enabled,
                @ProfilerConfigurationProperty(name = OVERFLOW_LIMIT, defaultValue = "20480") final long overflowLimitBytes,
                @ProfilerConfigurationProperty(name = MAX_ROTATIONS, defaultValue = "10") final int maxRotations,
                @ProfilerConfigurationProperty(name = ROTATION_MODE) RotationMode mode
        ) {
            this.enabled = enabled;
            this.overflowLimitBytes = overflowLimitBytes;
            this.maxRotations = maxRotations;
            this.mode = mode;
        }

        boolean enabled() {
            return enabled;
        }

        long overflowLimitBytes() {
            return overflowLimitBytes;
        }

        int maxRotations() {
            return maxRotations;
        }

        RotationMode mode() {
            return mode;
        }

        @Override
        public String toString() {
            return "Rotation{"
                    + "enabled=" + enabled
                    + ", overflowLimitBytes=" + overflowLimitBytes
                    + ", maxRotations=" + maxRotations
                    + ", mode=" + mode
                    + '}';
        }

        enum RotationMode {

            @ProfilerDefaultEnumProperty
            SYNC,

            ASYNC
        }
    }
}
