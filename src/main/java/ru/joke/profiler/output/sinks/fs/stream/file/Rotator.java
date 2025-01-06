package ru.joke.profiler.output.sinks.fs.stream.file;

import ru.joke.profiler.output.sinks.ProfilerOutputSinkException;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

final class Rotator {

    private static final Rotator instance = new Rotator();

    static Rotator getInstance() {
        return instance;
    }

    File rotate(
            final String path,
            final int maxRotations
    ) {
        final File mainFile = new File(path);
        if (maxRotations == 0) {
            return mainFile;
        }

        final String currentDate = LocalDate.now().toString();

        int index = 0;
        final List<File> existingFiles = new ArrayList<>(maxRotations == Integer.MAX_VALUE ? 10 : maxRotations);

        while (mainFile.exists()) {
            final String nextFilePath = path + "." + currentDate + "." + ++index;
            final File file = new File(nextFilePath);
            if (!file.exists() || index == maxRotations) {
                final File targetFile = renameChain(existingFiles, file);
                rename(mainFile, targetFile);
                break;
            } else {
                existingFiles.add(file);
            }
        }

        return mainFile;
    }

    private File renameChain(
            final List<File> chain,
            final File lastInChainFile
    ) {
        File currentFileInChain = lastInChainFile;
        for (int i = chain.size() - 1; i >= 0; i--) {
            rename(chain.get(i), currentFileInChain);
            currentFileInChain = chain.get(i);
        }

        return currentFileInChain;
    }

    private void rename(
            final File sourceFile,
            final File targetFile
    ) {
        if (!sourceFile.exists()) {
            throw new ProfilerOutputSinkException(String.format("File %s has been deleted or access to it has been lost", sourceFile.getAbsolutePath()));
        }

        if (targetFile.exists() && !targetFile.delete()) {
            throw new ProfilerOutputSinkException("Unable to delete existing file: " + targetFile.getAbsolutePath());
        }

        if (!sourceFile.renameTo(targetFile)) {
            throw new ProfilerOutputSinkException(String.format("Unable to rotate existing file %s to %s", sourceFile.getAbsolutePath(), targetFile.getAbsolutePath()));
        }
    }
}
