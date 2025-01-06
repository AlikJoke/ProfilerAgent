package ru.joke.profiler.output.sinks.fs.stream.file;

import ru.joke.profiler.configuration.meta.ProfilerDefaultEnumProperty;

import java.io.*;
import java.nio.charset.StandardCharsets;

public enum ExistingFilePolicy {

    @ProfilerDefaultEnumProperty
    REWRITE {
        @Override
        Writer createWriter(final String path) throws IOException {
            return new OutputStreamWriter(new FileOutputStream(path, false), StandardCharsets.UTF_8);
        }
    },

    ROTATE {
        @Override
        Writer createWriter(final String path) throws IOException {
            final File targetFile = Rotator.getInstance().rotate(path, Integer.MAX_VALUE);
            return new OutputStreamWriter(
                    new FileOutputStream(targetFile, false),
                    StandardCharsets.UTF_8
            );
        }
    },

    APPEND {
        @Override
        Writer createWriter(final String path) throws IOException {
            return new OutputStreamWriter(new FileOutputStream(path, true), StandardCharsets.UTF_8);
        }
    };

    abstract Writer createWriter(String path) throws IOException;
}
