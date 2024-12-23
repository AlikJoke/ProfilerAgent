package ru.joke.profiler.output.handlers.stream.file;

import ru.joke.profiler.output.handlers.ProfilerOutputSinkException;

import java.io.*;
import java.nio.charset.StandardCharsets;

public enum ExistingFilePolicy {

    REWRITE {
        @Override
        public Writer createWriter(String path) throws IOException {
            return new OutputStreamWriter(new FileOutputStream(path, false), StandardCharsets.UTF_8);
        }
    },

    ROTATE {
        @Override
        public Writer createWriter(String path) throws IOException {
            final File oldFile = new File(path);
            final File renamedFile = findNextAvailable(path);
            if (oldFile.exists()) {
                if (!oldFile.renameTo(renamedFile)) {
                    throw new ProfilerOutputSinkException(String.format("Unable to rotate existing file %s to %s", path, renamedFile.getAbsolutePath()));
                }
            }

            return new OutputStreamWriter(new FileOutputStream(path, false), StandardCharsets.UTF_8);
        }

        private File findNextAvailable(final String path) {
            int index = 0;
            while (true) {
                final File file = new File(path + "." + ++index);
                if (!file.exists()) {
                    return file;
                }
            }
        }
    },

    APPEND {
        @Override
        public Writer createWriter(String path) throws IOException {
            return new OutputStreamWriter(new FileOutputStream(path, true), StandardCharsets.UTF_8);
        }
    };

    public abstract Writer createWriter(final String path) throws IOException;

    public static ExistingFilePolicy parse(final String alias) {
        for (final ExistingFilePolicy policy : values()) {
            if (policy.name().equalsIgnoreCase(alias)) {
                return policy;
            }
        }

        return ExistingFilePolicy.REWRITE;
    }
}
