package ru.joke.profiler.output.sinks.fs.stream.file;

import ru.joke.profiler.configuration.meta.ProfilerDefaultEnumProperty;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static ru.joke.profiler.util.ArgUtil.checkNotEmpty;

public enum ExistingFilePolicy {

    @ProfilerDefaultEnumProperty
    REWRITE {
        @Override
        Writer createWriter(final String path) throws IOException {
            return new OutputStreamWriter(
                    new FileOutputStream(checkNotEmpty(path, "path"), false),
                    StandardCharsets.UTF_8
            );
        }
    },

    ROTATE {
        @Override
        Writer createWriter(final String path) throws IOException {
            final File targetFile = Rotator.getInstance().rotate(checkNotEmpty(path, "path"), Integer.MAX_VALUE);
            return new OutputStreamWriter(
                    new FileOutputStream(targetFile, false),
                    StandardCharsets.UTF_8
            );
        }
    },

    APPEND {
        @Override
        Writer createWriter(final String path) throws IOException {
            return new OutputStreamWriter(
                    new FileOutputStream(checkNotEmpty(path, "path"), true),
                    StandardCharsets.UTF_8
            );
        }
    };

    abstract Writer createWriter(String path) throws IOException;
}
